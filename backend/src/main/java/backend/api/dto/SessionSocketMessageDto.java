package backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionSocketMessageDto {
    private String type;
    private GameSessionResponseDto session;
    private DiceRollResponseDto diceRoll;
}