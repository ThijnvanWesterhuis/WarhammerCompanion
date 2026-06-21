package backend.api.dto;

public record MissionGenerationResponseDto(
        String missionName,
        String deploymentMap,
        String missionBriefing
) {
}