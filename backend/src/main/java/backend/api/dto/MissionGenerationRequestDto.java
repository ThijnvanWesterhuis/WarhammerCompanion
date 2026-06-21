package backend.api.dto;

public record MissionGenerationRequestDto(
        String playerOneFaction,
        String playerTwoFaction,
        String notes
) {
}