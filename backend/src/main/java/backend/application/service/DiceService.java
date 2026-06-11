package backend.application.service;

import backend.api.dto.*;
import backend.api.event.DiceRolledEvent;
import backend.api.exception.FieldValidationException;
import backend.data.repository.DicePresetRepository;
import backend.data.repository.DiceRollRepository;
import backend.data.repository.GameSessionRepository;
import backend.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class DiceService {

    private final DicePresetRepository dicePresetRepository;
    private final DiceRollRepository diceRollRepository;
    private final GameSessionRepository gameSessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<DicePresetResponseDto> getPresets(User user) {
        return dicePresetRepository.findByUserOrderByNameAsc(user)
                .stream()
                .map(DicePresetResponseDto::fromPreset)
                .toList();
    }

    public DicePresetResponseDto createPreset(User user, DicePresetRequestDto request) {
        validatePresetNameIsAvailable(user, request.getName(), null);
        validateSuccessThreshold(request.getDiceType(), request.getSuccessThreshold());

        DicePreset preset = DicePreset.builder()
                .user(user)
                .name(request.getName().trim())
                .diceType(request.getDiceType())
                .diceCount(request.getDiceCount())
                .successThreshold(request.getSuccessThreshold())
                .phase(trimOrNull(request.getPhase()))
                .build();

        DicePreset savedPreset = dicePresetRepository.save(preset);
        return DicePresetResponseDto.fromPreset(savedPreset);
    }

    public DicePresetResponseDto updatePreset(User user, Long presetId, DicePresetRequestDto request) {
        DicePreset preset = findPresetForUser(user, presetId);

        validatePresetNameIsAvailable(user, request.getName(), preset.getId());
        validateSuccessThreshold(request.getDiceType(), request.getSuccessThreshold());

        preset.setName(request.getName().trim());
        preset.setDiceType(request.getDiceType());
        preset.setDiceCount(request.getDiceCount());
        preset.setSuccessThreshold(request.getSuccessThreshold());
        preset.setPhase(trimOrNull(request.getPhase()));

        DicePreset savedPreset = dicePresetRepository.save(preset);
        return DicePresetResponseDto.fromPreset(savedPreset);
    }

    public void deletePreset(User user, Long presetId) {
        DicePreset preset = findPresetForUser(user, presetId);
        dicePresetRepository.delete(preset);
    }

    public DiceRollResponseDto rollDice(User user, DiceRollRequestDto request) {
        validateSuccessThreshold(request.getDiceType(), request.getSuccessThreshold());

        String sourcePresetName = null;

        if (request.getPresetId() != null) {
            DicePreset preset = findPresetForUser(user, request.getPresetId());
            sourcePresetName = preset.getName();
        }

        GameSession session = findActiveSessionForUserOrNull(user, request.getSessionId());

        List<Integer> results = rollNewResults(
                request.getDiceType().getSides(),
                request.getDiceCount()
        );

        DiceRoll roll = DiceRoll.builder()
                .user(user)
                .gameSession(session)
                .diceType(request.getDiceType())
                .diceCount(request.getDiceCount())
                .successThreshold(request.getSuccessThreshold())
                .results(results)
                .sourcePresetName(sourcePresetName)
                .build();

        DiceRoll savedRoll = diceRollRepository.save(roll);
        DiceRollResponseDto response = DiceRollResponseDto.fromRoll(savedRoll);

        publishDiceRollEventIfNeeded(savedRoll, response);

        return response;
    }

    public DiceRollResponseDto rerollLastRoll(User user, Long sessionId) {
        GameSession session = findActiveSessionForUserOrNull(user, sessionId);

        DiceRoll previousRoll = session == null
                ? diceRollRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalArgumentException("There is no previous roll to reroll"))
                : diceRollRepository.findFirstByUserAndGameSessionOrderByCreatedAtDesc(user, session)
                .orElseThrow(() -> new IllegalArgumentException("There is no previous roll in this session to reroll"));

        List<Integer> results = rollNewResults(
                previousRoll.getDiceType().getSides(),
                previousRoll.getDiceCount()
        );

        DiceRoll reroll = DiceRoll.builder()
                .user(user)
                .gameSession(previousRoll.getGameSession())
                .diceType(previousRoll.getDiceType())
                .diceCount(previousRoll.getDiceCount())
                .successThreshold(previousRoll.getSuccessThreshold())
                .results(results)
                .sourcePresetName(previousRoll.getSourcePresetName())
                .rerollSourceRollId(previousRoll.getId())
                .rerollType("FULL")
                .build();

        DiceRoll savedRoll = diceRollRepository.save(reroll);
        DiceRollResponseDto response = DiceRollResponseDto.fromRoll(savedRoll);

        publishDiceRollEventIfNeeded(savedRoll, response);

        return response;
    }

    public DiceRollResponseDto rerollDiceWithValue(User user, DiceRerollValueRequestDto request) {
        DiceRoll previousRoll = diceRollRepository.findByIdAndUser(request.getRollId(), user)
                .orElseThrow(() -> new IllegalArgumentException("Roll was not found"));

        if (previousRoll.getGameSession() != null
                && previousRoll.getGameSession().getStatus() != GameSessionStatus.ACTIVE) {
            throw new IllegalArgumentException("This session is already finished");
        }

        int sides = previousRoll.getDiceType().getSides();

        if (request.getRerollValue() > sides) {
            throw new FieldValidationException(
                    "rerollValue",
                    "Reroll value may not be higher than " + sides + " for " + previousRoll.getDiceType()
            );
        }

        List<Integer> results = new ArrayList<>(previousRoll.getResults());
        boolean foundValueToReroll = false;

        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).equals(request.getRerollValue())) {
                results.set(i, rollSingleDie(sides));
                foundValueToReroll = true;
            }
        }

        if (!foundValueToReroll) {
            throw new FieldValidationException(
                    "rerollValue",
                    "No dice with value " + request.getRerollValue() + " were found in this roll"
            );
        }

        DiceRoll reroll = DiceRoll.builder()
                .user(user)
                .gameSession(previousRoll.getGameSession())
                .diceType(previousRoll.getDiceType())
                .diceCount(previousRoll.getDiceCount())
                .successThreshold(previousRoll.getSuccessThreshold())
                .results(results)
                .sourcePresetName(previousRoll.getSourcePresetName())
                .rerollSourceRollId(previousRoll.getId())
                .rerollType("VALUE_" + request.getRerollValue())
                .build();

        DiceRoll savedRoll = diceRollRepository.save(reroll);
        DiceRollResponseDto response = DiceRollResponseDto.fromRoll(savedRoll);

        publishDiceRollEventIfNeeded(savedRoll, response);

        return response;
    }

    public List<DiceRollResponseDto> getRollHistory(User user) {
        return diceRollRepository.findTop20ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(DiceRollResponseDto::fromRoll)
                .toList();
    }

    public List<DiceRollResponseDto> getSessionRollHistory(User user, Long sessionId) {
        GameSession session = findSessionForUser(user, sessionId);

        return diceRollRepository.findTop20ByUserAndGameSessionOrderByCreatedAtDesc(user, session)
                .stream()
                .map(DiceRollResponseDto::fromRoll)
                .toList();
    }

    private void publishDiceRollEventIfNeeded(DiceRoll roll, DiceRollResponseDto response) {
        if (roll.getGameSession() != null) {
            eventPublisher.publishEvent(new DiceRolledEvent(
                    roll.getGameSession().getId(),
                    response
            ));
        }
    }

    private GameSession findActiveSessionForUserOrNull(User user, Long sessionId) {
        if (sessionId == null) {
            return null;
        }

        GameSession session = findSessionForUser(user, sessionId);

        if (session.getStatus() != GameSessionStatus.ACTIVE) {
            throw new IllegalArgumentException("This session is already finished");
        }

        return session;
    }

    private GameSession findSessionForUser(User user, Long sessionId) {
        return gameSessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new IllegalArgumentException("Session was not found"));
    }

    private List<Integer> rollNewResults(int sides, int diceCount) {
        List<Integer> results = new ArrayList<>();

        for (int i = 0; i < diceCount; i++) {
            results.add(rollSingleDie(sides));
        }

        return results;
    }

    private int rollSingleDie(int sides) {
        return ThreadLocalRandom.current().nextInt(1, sides + 1);
    }

    private void validateSuccessThreshold(DiceType diceType, Integer successThreshold) {
        if (successThreshold == null) {
            return;
        }

        if (successThreshold < 1 || successThreshold > diceType.getSides()) {
            throw new FieldValidationException(
                    "successThreshold",
                    "Success threshold must be between 1 and " + diceType.getSides() + " for " + diceType
            );
        }
    }

    private DicePreset findPresetForUser(User user, Long presetId) {
        return dicePresetRepository.findByIdAndUser(presetId, user)
                .orElseThrow(() -> new IllegalArgumentException("Preset was not found"));
    }

    private void validatePresetNameIsAvailable(User user, String name, Long currentPresetId) {
        String trimmedName = name.trim();

        boolean nameExists = dicePresetRepository.existsByUserAndNameIgnoreCase(user, trimmedName);

        if (!nameExists) {
            return;
        }

        if (currentPresetId != null) {
            DicePreset currentPreset = findPresetForUser(user, currentPresetId);

            if (currentPreset.getName().equalsIgnoreCase(trimmedName)) {
                return;
            }
        }

        throw new FieldValidationException("name", "Preset name is already in use");
    }

    private String trimOrNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }
}