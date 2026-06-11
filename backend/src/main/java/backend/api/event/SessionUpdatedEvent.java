package backend.api.event;

import backend.api.dto.GameSessionResponseDto;

public record SessionUpdatedEvent(
        Long sessionId,
        String updateType,
        GameSessionResponseDto session
) {
}