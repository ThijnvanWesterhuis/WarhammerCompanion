import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { GameSession } from '../../models/session.models';
import { SessionService } from '../../services/session.service';

@Component({
  selector: 'app-match-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './match-edit.html',
  styleUrl: './match-edit.css'
})
export class MatchEdit implements OnInit {
  match = signal<GameSession | null>(null);
  loading = signal(false);
  saving = signal(false);
  errorMessage = signal('');

  playerOneName = '';
  playerTwoName = '';
  playerOneFaction = '';
  playerTwoFaction = '';
  missionName = '';
  deploymentMap = '';
  notes = '';
  playerOneScore = 0;
  playerTwoScore = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sessionService: SessionService
  ) {}

  ngOnInit() {
    this.loadMatch();
  }

  saveMatch() {
    const currentMatch = this.match();

    if (!currentMatch || this.saving()) {
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');

    this.sessionService.updateMatch(currentMatch.id, {
      playerOneName: this.cleanOptional(this.playerOneName),
      playerTwoName: this.cleanOptional(this.playerTwoName),
      playerOneFaction: this.cleanOptional(this.playerOneFaction),
      playerTwoFaction: this.cleanOptional(this.playerTwoFaction),
      missionName: this.cleanOptional(this.missionName),
      deploymentMap: this.cleanOptional(this.deploymentMap),
      notes: this.cleanOptional(this.notes),
      playerOneScore: this.playerOneScore,
      playerTwoScore: this.playerTwoScore
    }).subscribe({
      next: updatedMatch => this.router.navigate(['/matches', updatedMatch.id]),
      error: error => this.handleError(error, 'Could not update match')
    });
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
        this.fillForm(match);
        this.loading.set(false);
      },
      error: error => this.handleError(error, 'Could not load match')
    });
  }

  private fillForm(match: GameSession) {
    this.playerOneName = match.playerOneName ?? '';
    this.playerTwoName = match.playerTwoName ?? '';
    this.playerOneFaction = match.playerOneFaction ?? '';
    this.playerTwoFaction = match.playerTwoFaction ?? '';
    this.missionName = match.missionName ?? '';
    this.deploymentMap = match.deploymentMap ?? '';
    this.notes = match.notes ?? '';
    this.playerOneScore = match.playerOneScore;
    this.playerTwoScore = match.playerTwoScore;
  }

  private cleanOptional(value: string) {
    const trimmedValue = value.trim();
    return trimmedValue ? trimmedValue : null;
  }

  private handleError(error: HttpErrorResponse, fallbackMessage: string) {
    const response = error.error as Record<string, string> | string | null;

    this.loading.set(false);
    this.saving.set(false);

    if (response && typeof response === 'object') {
      this.errorMessage.set(response['error'] ?? Object.values(response)[0] ?? fallbackMessage);
      return;
    }

    if (typeof response === 'string') {
      this.errorMessage.set(response);
      return;
    }

    this.errorMessage.set(fallbackMessage);
  }
}
