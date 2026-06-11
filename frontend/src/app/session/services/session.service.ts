import { Injectable } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import {
  GameSession,
  GameSessionRequest,
  GameSessionUpdateRequest,
  MatchResult,
  PageResponse
} from '../models/session.models';

@Injectable({ providedIn: 'root' })
export class SessionService {
  constructor(private api: ApiService) {}

  startSession(request: GameSessionRequest) {
    return this.api.post<GameSession>('/sessions/start', request);
  }

  getActiveSession() {
    return this.api.get<GameSession | null>('/sessions/active');
  }

  getSessions() {
    return this.api.get<GameSession[]>('/sessions');
  }

  getMatchHistory(page: number, size: number, search = '', faction = '', result = '') {
    const params = new URLSearchParams();
    params.set('page', page.toString());
    params.set('size', size.toString());

    if (search.trim()) {
      params.set('search', search.trim());
    }

    if (faction.trim()) {
      params.set('faction', faction.trim());
    }

    if (result.trim()) {
      params.set('result', result.trim() as MatchResult);
    }

    return this.api.get<PageResponse<GameSession>>(`/sessions/history?${params.toString()}`);
  }

  getSession(sessionId: number) {
    return this.api.get<GameSession>(`/sessions/${sessionId}`);
  }

  updateScore(sessionId: number, playerOneScore: number, playerTwoScore: number) {
    return this.api.patch<GameSession>(`/sessions/${sessionId}/score`, {
      playerOneScore,
      playerTwoScore
    });
  }

  updateRound(sessionId: number, currentRound: number) {
    return this.api.patch<GameSession>(`/sessions/${sessionId}/round`, {
      currentRound
    });
  }

  endSession(sessionId: number) {
    return this.api.patch<GameSession>(`/sessions/${sessionId}/end`, {});
  }

  updateMatch(sessionId: number, request: GameSessionUpdateRequest) {
    return this.api.patch<GameSession>(`/sessions/${sessionId}`, request);
  }

  deleteMatch(sessionId: number) {
    return this.api.delete<void>(`/sessions/${sessionId}`);
  }
}
