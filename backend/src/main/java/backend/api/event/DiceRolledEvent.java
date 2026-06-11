package backend.api.event;

import backend.api.dto.DiceRollResponseDto;

public record DiceRolledEvent(Long sessionId, DiceRollResponseDto diceRoll) {
}