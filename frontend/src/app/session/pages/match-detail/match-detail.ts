import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { GameSession } from '../../models/session.models';
import { SessionService } from '../../services/session.service';

@Component({
  selector: 'app-match-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './match-detail.html',
  styleUrl: './match-detail.css'
})
export class MatchDetail implements OnInit {
  match = signal<GameSession | null>(null);
  loading = signal(false);
  deleting = signal(false);
  errorMessage = signal('');

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sessionService: SessionService
  ) {}

  ngOnInit() {
    this.loadMatch();
  }

  deleteMatch() {
    const currentMatch = this.match();

    if (!currentMatch || this.deleting()) {
      return;
    }

    const confirmed = confirm('Are you sure you want to delete this saved match?');

    if (!confirmed) {
      return;
    }

    this.deleting.set(true);
    this.errorMessage.set('');

    this.sessionService.deleteMatch(currentMatch.id).subscribe({
      next: () => this.router.navigate(['/matches']),
      error: error => this.handleError(error, 'Could not delete match')
    });
  }

  getResultLabel(match: GameSession) {
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

  getOpponent(match: GameSession) {
    if (match.playerTwoName && match.playerTwoFaction) {
      return `${match.playerTwoName} (${match.playerTwoFaction})`;
    }

    return match.playerTwoName || match.playerTwoFaction || 'Unknown opponent';
  }

  formatDuration(totalSeconds: number) {
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    return [hours, minutes, seconds]
      .map(value => value.toString().padStart(2, '0'))
      .join(':');
  }

  private loadMatch() {
    const sessionId = Number(this.route.snapshot.paramMap.get('id'));

    if (!sessionId) {
      this.errorMessage.set('Invalid match id');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.sessionService.getSession(sessionId).subscribe({
      next: match => {
        this.match.set(match);
        this.loading.set(false);
      },
      error: error => this.handleError(error, 'Could not load match')
    });
  }

  private handleError(error: HttpErrorResponse, fallbackMessage: string) {
    const response = error.error as Record<string, string> | string | null;

    this.loading.set(false);
    this.deleting.set(false);

    if (response && typeof response === 'object') {
      this.errorMessage.set(response['error'] ?? fallbackMessage);
      return;
    }

    if (typeof response === 'string') {
      this.errorMessage.set(response);
      return;
    }

    this.errorMessage.set(fallbackMessage);
  }
}
