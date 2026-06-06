package backend.application.service;

import backend.api.dto.*;
import backend.api.exception.FieldValidationException;
import backend.data.repository.DicePresetRepository;
import backend.data.repository.DiceRollRepository;
import backend.domain.DicePreset;
import backend.domain.DiceRoll;
import backend.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class DiceService {

    private final DicePresetRepository dicePresetRepository;
    private final DiceRollRepository diceRollRepository;

    public List<DicePresetResponseDto> getPresets(User user) {
        return dicePresetRepository.findByUserOrderByNameAsc(user)
                .stream()
                .map(DicePresetResponseDto::fromPreset)
                .toList();
    }

    public DicePresetResponseDto createPreset(User user, DicePresetRequestDto request) {
        validatePresetNameIsAvailable(user, request.getName(), null);

        DicePreset preset = DicePreset.builder()
                .user(user)
                .name(request.getName().trim())
                .diceType(request.getDiceType())
                .diceCount(request.getDiceCount())
                .phase(trimOrNull(request.getPhase()))
                .build();

        DicePreset savedPreset = dicePresetRepository.save(preset);
        return DicePresetResponseDto.fromPreset(savedPreset);
    }

    public DicePresetResponseDto updatePreset(User user, Long presetId, DicePresetRequestDto request) {
        DicePreset preset = findPresetForUser(user, presetId);
        validatePresetNameIsAvailable(user, request.getName(), preset.getId());

        preset.setName(request.getName().trim());
        preset.setDiceType(request.getDiceType());
        preset.setDiceCount(request.getDiceCount());
        preset.setPhase(trimOrNull(request.getPhase()));

        DicePreset savedPreset = dicePresetRepository.save(preset);
        return DicePresetResponseDto.fromPreset(savedPreset);
    }

    public void deletePreset(User user, Long presetId) {
        DicePreset preset = findPresetForUser(user, presetId);
        dicePresetRepository.delete(preset);
    }

    public DiceRollResponseDto rollDice(User user, DiceRollRequestDto request) {
        String sourcePresetName = null;

        if (request.getPresetId() != null) {
            DicePreset preset = findPresetForUser(user, request.getPresetId());
            sourcePresetName = preset.getName();
        }

        List<Integer> results = new ArrayList<>();
        int sides = request.getDiceType().getSides();

        for (int i = 0; i < request.getDiceCount(); i++) {
            results.add(ThreadLocalRandom.current().nextInt(1, sides + 1));
        }

        DiceRoll roll = DiceRoll.builder()
                .user(user)
                .diceType(request.getDiceType())
                .diceCount(request.getDiceCount())
                .results(results)
                .sourcePresetName(sourcePresetName)
                .build();

        DiceRoll savedRoll = diceRollRepository.save(roll);
        return DiceRollResponseDto.fromRoll(savedRoll);
    }

    public List<DiceRollResponseDto> getRollHistory(User user) {
        return diceRollRepository.findTop20ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(DiceRollResponseDto::fromRoll)
                .toList();
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