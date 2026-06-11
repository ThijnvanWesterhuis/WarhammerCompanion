package backend.application.service;

import backend.api.dto.SessionSocketMessageDto;
import backend.api.event.DiceRolledEvent;
import backend.api.event.SessionStartedEvent;
import backend.api.event.SessionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionWebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionStarted(SessionStartedEvent event) {
        send(event.sessionId(), new SessionSocketMessageDto(
                "SESSION_STARTED",
                event.session(),
                null
        ));
    }

    @EventListener
    public void handleSessionUpdated(SessionUpdatedEvent event) {
        send(event.sessionId(), new SessionSocketMessageDto(
                event.updateType(),
                event.session(),
                null
        ));
    }

    @EventListener
    public void handleDiceRolled(DiceRolledEvent event) {
        send(event.sessionId(), new SessionSocketMessageDto(
                "DICE_ROLLED",
                null,
                event.diceRoll()
        ));
    }

    private void send(Long sessionId, SessionSocketMessageDto message) {
        messagingTemplate.convertAndSend("/topic/sessions/" + sessionId, message);
    }
}