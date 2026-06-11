import { DiceRoll } from '../../dice/models/dice.models';

export type GameSessionStatus = 'ACTIVE' | 'FINISHED';
export type MatchResult = 'IN_PROGRESS' | 'VICTORY' | 'DEFEAT' | 'DRAW';

export interface GameSessionRequest {
  playerOneName?: string | null;
  playerTwoName?: string | null;
  playerOneFaction?: string | null;
  playerTwoFaction?: string | null;
  missionName?: string | null;
  deploymentMap?: string | null;
  notes?: string | null;
}

export interface GameSessionUpdateRequest extends GameSessionRequest {
  playerOneScore: number;
  playerTwoScore: number;
}

export interface GameSessionRoundScore {
  id: number;
  roundNumber: number;
  playerOneScore: number;
  playerTwoScore: number;
  updatedAt: string;
}

export interface GameSession {
  id: number;
  playerOneName?: string | null;
  playerTwoName?: string | null;
  playerOneFaction?: string | null;
  playerTwoFaction?: string | null;
  missionName?: string | null;
  deploymentMap?: string | null;
  notes?: string | null;
  currentRound: number;
  playerOneScore: number;
  playerTwoScore: number;
  status: GameSessionStatus;
  result: MatchResult;
  elapsedSeconds: number;
  startedAt: string;
  endedAt?: string | null;
  roundScores: GameSessionRoundScore[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface SessionSocketMessage {
  type: 'SESSION_STARTED' | 'SCORE_UPDATED' | 'ROUND_UPDATED' | 'SESSION_ENDED' | 'DICE_ROLLED';
  session?: GameSession | null;
  diceRoll?: DiceRoll | null;
}
