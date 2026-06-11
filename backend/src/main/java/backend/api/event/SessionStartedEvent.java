package backend.api.event;

import backend.api.dto.GameSessionResponseDto;

public record SessionStartedEvent(Long sessionId, GameSessionResponseDto session) {
}