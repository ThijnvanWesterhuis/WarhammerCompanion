import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { DiceRoll, DiceType } from '../../../dice/models/dice.models';
import { DiceService } from '../../../dice/services/dice.service';
import { GameSession, SessionSocketMessage } from '../../models/session.models';
import { SessionService } from '../../services/session.service';
import { RealtimeSessionService } from '../../services/realtime-session.service';
import { WARHAMMER_40K_FACTIONS } from '../../../shared/warhammer-factions';

@Component({
  selector: 'app-active-session',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './active-session.html',
  styleUrl: './active-session.css'
})
export class ActiveSession implements OnInit, OnDestroy {
  readonly diceTypes: DiceType[] = ['D6', 'D10', 'D20'];
  readonly roundNumbers = [1, 2, 3, 4, 5];
  readonly warhammerFactions = WARHAMMER_40K_FACTIONS;

  playerOneName = '';
  playerTwoName = '';
  playerOneFaction = '';
  playerTwoFaction = '';
  notes = '';
  missionName = '';
  deploymentMap = '';

  diceType: DiceType = 'D6';
  diceCount = 5;
  successThreshold: number | null = 4;

  session = signal<GameSession | null>(null);
  rollHistory = signal<DiceRoll[]>([]);
  lastRoll = signal<DiceRoll | null>(null);

  elapsedSeconds = signal(0);
  showSaveSummary = signal(false);

  errorMessage = signal('');
  successMessage = signal('');

  private realtimeSubscription?: Subscription;
  private timerIntervalId?: number;

  constructor(
    private sessionService: SessionService,
    private diceService: DiceService,
    private realtimeSessionService: RealtimeSessionService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadActiveSession();
  }

  ngOnDestroy() {
    this.realtimeSubscription?.unsubscribe();
    this.stopTimer();
  }

  get diceSides() {
    return Number(this.diceType.replace('D', ''));
  }

  get thresholdOptions() {
    return Array.from({ length: this.diceSides }, (_, index) => index + 1);
  }

  get canRerollOnes() {
    return this.getLatestRollWithOnes() !== null;
  }

  getFormattedElapsedTime() {
    return this.formatDuration(this.elapsedSeconds());
  }

  formatRound(round: number) {
    return round.toString().padStart(2, '0');
  }

  startSession() {
    this.clearMessages();

    this.sessionService.startSession({
      playerOneName: this.playerOneName.trim() || null,
      playerTwoName: this.playerTwoName.trim() || null,
      playerOneFaction: this.playerOneFaction.trim() || null,
      playerTwoFaction: this.playerTwoFaction.trim() || null,
      missionName: this.missionName.trim() || null,
      deploymentMap: this.deploymentMap.trim() || null,
      notes: this.notes.trim() || null
    }).subscribe({
      next: session => {
        this.setActiveSession(session);
        this.successMessage.set('Session started.');
      },
      error: error => this.handleError(error, 'Could not start session')
    });
  }

  updateScore(playerOneScore: number, playerTwoScore: number) {
    const session = this.session();

    if (!session) {
      return;
    }

    this.sessionService.updateScore(session.id, playerOneScore, playerTwoScore).subscribe({
      next: updatedSession => this.setSessionFromServer(updatedSession),
      error: error => this.handleError(error, 'Could not update score')
    });
  }

  updateRound(currentRound: number) {
    const session = this.session();

    if (!session) {
      return;
    }

    if (currentRound < 1 || currentRound > this.roundNumbers.length) {
      return;
    }

    this.sessionService.updateRound(session.id, currentRound).subscribe({
      next: updatedSession => this.setSessionFromServer(updatedSession),
      error: error => this.handleError(error, 'Could not update round')
    });
  }

  openSaveSummary() {
    this.clearMessages();

    const currentSession = this.session();

    if (currentSession) {
      this.elapsedSeconds.set(this.calculateElapsedSeconds(currentSession));
    }

    this.stopTimer();
    this.showSaveSummary.set(true);
  }

  closeSaveSummary() {
    this.showSaveSummary.set(false);

    const currentSession = this.session();

    if (currentSession && currentSession.status !== 'FINISHED') {
      this.startTimer(currentSession);
    }
  }

  endSession() {
    this.openSaveSummary();
  }

  confirmEndSession() {
    const session = this.session();

    if (!session) {
      return;
    }

    this.sessionService.endSession(session.id).subscribe({
      next: updatedSession => {
        this.setSessionFromServer(updatedSession);
        this.realtimeSubscription?.unsubscribe();
        this.stopTimer();
        this.showSaveSummary.set(false);
        this.successMessage.set('Session saved to match history.');
        this.router.navigate(['/matches']);
      },
      error: error => this.handleError(error, 'Could not save session')
    });
  }

  rollDice() {
    const session = this.session();

    if (!session) {
      this.errorMessage.set('Start a session before rolling dice in a session.');
      return;
    }

    this.clearMessages();

    this.diceService.roll({
      diceType: this.diceType,
      diceCount: this.diceCount,
      successThreshold: this.successThreshold,
      sessionId: session.id
    }).subscribe({
      next: roll => this.applyRoll(roll),
      error: error => this.handleError(error, 'Could not roll dice')
    });
  }

  rerollLastRoll() {
    const session = this.session();

    if (!session) {
      return;
    }

    this.clearMessages();

    this.diceService.rerollLast(session.id).subscribe({
      next: roll => this.applyRoll(roll),
      error: error => this.handleError(error, 'Could not reroll last roll')
    });
  }

  rerollAllOnes() {
    const roll = this.getLatestRollWithOnes();

    if (!roll) {
      this.errorMessage.set('There are no 1s in the latest roll to reroll.');
      return;
    }

    this.clearMessages();

    this.diceService.rerollValue({
      rollId: roll.id,
      rerollValue: 1
    }).subscribe({
      next: reroll => {
        this.applyRoll(reroll);
        this.successMessage.set('Rerolled all dice that showed a 1.');
      },
      error: error => this.handleError(error, 'Could not reroll ones')
    });
  }

  private getLatestRollWithOnes() {
    const currentLastRoll = this.lastRoll();

    if (currentLastRoll?.results.includes(1)) {
      return currentLastRoll;
    }

    return null;
  }

  isSuccess(result: number, roll: DiceRoll) {
    return roll.successThreshold !== null
      && roll.successThreshold !== undefined
      && result >= roll.successThreshold;
  }

  isFail(result: number, roll: DiceRoll) {
    return roll.successThreshold !== null
      && roll.successThreshold !== undefined
      && result < roll.successThreshold;
  }

  getRoundScore(roundNumber: number, player: 'one' | 'two') {
    const roundScore = this.session()?.roundScores?.find(score => score.roundNumber === roundNumber);

    if (!roundScore) {
      return '-';
    }

    return player === 'one'
      ? roundScore.playerOneScore
      : roundScore.playerTwoScore;
  }

  getResultLabel(session: GameSession) {
    if (session.playerOneScore > session.playerTwoScore) {
      return 'Victory';
    }

    if (session.playerOneScore < session.playerTwoScore) {
      return 'Defeat';
    }

    return 'Draw';
  }

  getResultTagClass(session: GameSession) {
    if (session.playerOneScore > session.playerTwoScore) {
      return 'warning';
    }

    if (session.playerOneScore < session.playerTwoScore) {
      return 'danger';
    }

    return '';
  }

  formatDateTime(value?: string | null) {
    if (!value) {
      return '-';
    }

    return this.parseBackendDateTime(value).toLocaleString();
  }

  private loadActiveSession() {
    this.sessionService.getActiveSession().subscribe({
      next: session => {
        if (session) {
          this.setActiveSession(session);
        }
      },
      error: error => this.handleError(error, 'Could not load active session')
    });
  }

  private setActiveSession(session: GameSession) {
    this.setSessionFromServer(session);
    this.loadSessionRollHistory(session.id);
    this.connectToRealtimeSession(session.id);
    this.startTimer(session);
  }

  private setSessionFromServer(session: GameSession) {
    this.session.set(session);
    this.elapsedSeconds.set(this.calculateElapsedSeconds(session));

    if (session.status === 'FINISHED') {
      this.stopTimer();
    }
  }

  private loadSessionRollHistory(sessionId: number) {
    this.diceService.getSessionRollHistory(sessionId).subscribe({
      next: history => {
        this.rollHistory.set(history);
        this.lastRoll.set(history[0] ?? null);
      },
      error: error => this.handleError(error, 'Could not load session roll history')
    });
  }

  private connectToRealtimeSession(sessionId: number) {
    this.realtimeSubscription?.unsubscribe();

    this.realtimeSubscription = this.realtimeSessionService
      .connectToSession(sessionId)
      .subscribe({
        next: message => this.handleRealtimeMessage(message),
        error: () => this.errorMessage.set('Live connection failed')
      });
  }

  private handleRealtimeMessage(message: SessionSocketMessage) {
    if (message.session) {
      this.setSessionFromServer(message.session);
    }

    if (message.diceRoll) {
      this.applyRoll(message.diceRoll);
    }
  }

  private applyRoll(roll: DiceRoll) {
    this.lastRoll.set(roll);
    this.rollHistory.update(history => [
      roll,
      ...history.filter(existingRoll => existingRoll.id !== roll.id)
    ].slice(0, 20));

    this.successMessage.set(`Rolled ${roll.diceCount} ${roll.diceType}.`);
  }

  private startTimer(session: GameSession) {
    this.stopTimer();
    this.elapsedSeconds.set(this.calculateElapsedSeconds(session));

    if (session.status === 'FINISHED') {
      return;
    }

    this.timerIntervalId = window.setInterval(() => {
      const currentSession = this.session();

      if (!currentSession) {
        return;
      }

      this.elapsedSeconds.set(this.calculateElapsedSeconds(currentSession));
    }, 1000);
  }

  private stopTimer() {
    if (this.timerIntervalId !== undefined) {
      window.clearInterval(this.timerIntervalId);
      this.timerIntervalId = undefined;
    }
  }

  private calculateElapsedSeconds(session: GameSession) {
    if (!session.startedAt) {
      return session.elapsedSeconds ?? 0;
    }

    const startTime = this.parseBackendDateTime(session.startedAt).getTime();
    const endTime = session.endedAt
      ? this.parseBackendDateTime(session.endedAt).getTime()
      : Date.now();

    return Math.max(0, Math.floor((endTime - startTime) / 1000));
  }

  private parseBackendDateTime(value: string) {
    const alreadyHasTimezone = /(?:Z|[+-]\d{2}:\d{2})$/.test(value);

    if (alreadyHasTimezone) {
      return new Date(value);
    }

    return new Date(`${value}Z`);
  }

  private formatDuration(totalSeconds: number) {
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    return [hours, minutes, seconds]
      .map(value => value.toString().padStart(2, '0'))
      .join(':');
  }

  private clearMessages() {
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  private handleError(error: HttpErrorResponse, fallbackMessage: string) {
    const response = error.error as Record<string, string> | string | null;

    if (response && typeof response === 'object') {
      this.errorMessage.set(
        response['playerOneName']
        ?? response['playerTwoName']
        ?? response['playerOneFaction']
        ?? response['playerTwoFaction']
        ?? response['notes']
        ?? response['playerOneScore']
        ?? response['playerTwoScore']
        ?? response['currentRound']
        ?? response['diceCount']
        ?? response['diceType']
        ?? response['successThreshold']
        ?? response['rerollValue']
        ?? response['error']
        ?? fallbackMessage
      );
      return;
    }

    if (typeof response === 'string') {
      this.errorMessage.set(response);
      return;
    }

    this.errorMessage.set(fallbackMessage);
  }
}
