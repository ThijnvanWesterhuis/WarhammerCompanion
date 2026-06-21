import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AccountService } from '../auth/services/account.service';
import { GameSession } from '../session/models/session.models';
import { SessionService } from '../session/services/session.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {
  recentMatches = signal<GameSession[]>([]);
  loadingRecentMatches = signal(false);
  recentMatchesError = signal('');

  constructor(
    public accountService: AccountService,
    private sessionService: SessionService
  ) {}

  ngOnInit() {
    if (this.accountService.isLoggedIn()) {
      this.loadRecentMatches();
    }
  }

  getFinalScore(match: GameSession) {
    return `${match.playerOneScore} - ${match.playerTwoScore}`;
  }

  getMatchTitle(match: GameSession) {
    const playerOneFaction = match.playerOneFaction || 'Unknown faction';
    const playerTwoFaction = match.playerTwoFaction || 'Unknown faction';

    return `${playerOneFaction} vs ${playerTwoFaction}`;
  }

  getMatchSubtitle(match: GameSession) {
    const opponent = match.playerTwoName || 'Unknown opponent';
    return `Opponent: ${opponent}`;
  }

  formatResult(match: GameSession) {
    if (match.result === 'VICTORY') {
      return 'Victory';
    }

    if (match.result === 'DEFEAT') {
      return 'Defeat';
    }

    return 'Draw';
  }

  getResultClass(match: GameSession) {
    if (match.result === 'VICTORY') {
      return 'warning';
    }

    if (match.result === 'DEFEAT') {
      return 'danger';
    }

    return '';
  }

  private loadRecentMatches() {
    this.loadingRecentMatches.set(true);
    this.recentMatchesError.set('');

    this.sessionService.getMatchHistory(0, 2).subscribe({
      next: response => {
        this.recentMatches.set(response.content);
        this.loadingRecentMatches.set(false);
      },
      error: error => this.handleRecentMatchesError(error)
    });
  }

  private handleRecentMatchesError(error: HttpErrorResponse) {
    const response = error.error as Record<string, string> | string | null;

    this.loadingRecentMatches.set(false);

    if (response && typeof response === 'object') {
      this.recentMatchesError.set(response['error'] ?? 'Could not load recent matches');
      return;
    }

    if (typeof response === 'string') {
      this.recentMatchesError.set(response);
      return;
    }

    this.recentMatchesError.set('Could not load recent matches');
  }
}
