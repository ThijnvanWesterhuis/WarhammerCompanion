import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { GameSession } from '../../models/session.models';
import { SessionService } from '../../services/session.service';

@Component({
  selector: 'app-match-history',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './match-history.html',
  styleUrl: './match-history.css'
})
export class MatchHistory implements OnInit {
  readonly pageSize = 10;
  readonly resultOptions = ['', 'VICTORY', 'DEFEAT', 'DRAW'];

  search = '';
  faction = '';
  result = '';

  matches = signal<GameSession[]>([]);
  page = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  loading = signal(false);
  errorMessage = signal('');

  constructor(private sessionService: SessionService) {}

  ngOnInit() {
    this.loadMatches();
  }

  applyFilters() {
    this.page.set(0);
    this.loadMatches();
  }

  clearFilters() {
    this.search = '';
    this.faction = '';
    this.result = '';
    this.page.set(0);
    this.loadMatches();
  }

  nextPage() {
    if (this.page() + 1 >= this.totalPages()) {
      return;
    }

    this.page.update(value => value + 1);
    this.loadMatches();
  }

  previousPage() {
    if (this.page() <= 0) {
      return;
    }

    this.page.update(value => value - 1);
    this.loadMatches();
  }

  deleteMatch(match: GameSession) {
    const confirmed = confirm('Are you sure you want to delete this saved match?');

    if (!confirmed) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.sessionService.deleteMatch(match.id).subscribe({
      next: () => this.loadMatches(),
      error: error => this.handleError(error)
    });
  }

  getFinalScore(match: GameSession) {
    return `${match.playerOneScore} - ${match.playerTwoScore}`;
  }

  getOwnFaction(match: GameSession) {
    return match.playerOneFaction || 'Unknown faction';
  }

  getOpponent(match: GameSession) {
    if (match.playerTwoName && match.playerTwoFaction) {
      return `${match.playerTwoName} (${match.playerTwoFaction})`;
    }

    return match.playerTwoName || match.playerTwoFaction || 'Unknown opponent';
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

  formatResult(match: GameSession) {
    if (match.result === 'VICTORY') {
      return 'Victory';
    }

    if (match.result === 'DEFEAT') {
      return 'Defeat';
    }

    return 'Draw';
  }

  private loadMatches() {
    this.loading.set(true);
    this.errorMessage.set('');

    this.sessionService.getMatchHistory(
      this.page(),
      this.pageSize,
      this.search,
      this.faction,
      this.result
    ).subscribe({
      next: response => {
        this.matches.set(response.content);
        this.page.set(response.page);
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        this.loading.set(false);
      },
      error: error => this.handleError(error)
    });
  }

  private handleError(error: HttpErrorResponse) {
    const response = error.error as Record<string, string> | string | null;

    this.loading.set(false);

    if (response && typeof response === 'object') {
      this.errorMessage.set(response['error'] ?? 'Could not load match history');
      return;
    }

    if (typeof response === 'string') {
      this.errorMessage.set(response);
      return;
    }

    this.errorMessage.set('Could not load match history');
  }
}
