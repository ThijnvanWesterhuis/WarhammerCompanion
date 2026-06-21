import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Client } from '@stomp/stompjs';
import { environment } from '../../../environments/environment';
import { SessionSocketMessage } from '../models/session.models';

@Injectable({ providedIn: 'root' })
export class RealtimeSessionService {
  connectToSession(sessionId: number): Observable<SessionSocketMessage> {
    return new Observable<SessionSocketMessage>(observer => {
      const token = localStorage.getItem('token');

      const client = new Client({
        brokerURL: environment.wsUrl,
        connectHeaders: token
          ? { Authorization: `Bearer ${token}` }
          : {},
        reconnectDelay: 5000,

        onConnect: () => {
          client.subscribe(`/topic/sessions/${sessionId}`, message => {
            observer.next(JSON.parse(message.body) as SessionSocketMessage);
          });
        },

        onStompError: frame => {
          observer.error(frame.headers['message'] ?? 'WebSocket error');
        },

        onWebSocketError: event => {
          observer.error(event);
        }
      });

      client.activate();

      return () => {
        client.deactivate();
      };
    });
  }
}
