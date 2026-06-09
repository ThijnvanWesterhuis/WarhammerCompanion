import { Component, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { DicePreset, DiceRoll, DiceType } from '../../models/dice.models';
import { DiceService } from '../../services/dice.service';

@Component({
  selector: 'app-dice',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './dice.html',
  styleUrl: './dice.css'
})
export class Dice implements OnInit {
  readonly diceTypes: DiceType[] = ['D6', 'D10', 'D20'];

  diceType: DiceType = 'D6';
  diceCount = 5;
  successThreshold: number | null = 4;
  presetName = '';
  presetPhase = '';
  selectedPresetId: number | null = null;

  presets = signal<DicePreset[]>([]);
  rollHistory = signal<DiceRoll[]>([]);
  lastRoll = signal<DiceRoll | null>(null);
  errorMessage = signal('');
  successMessage = signal('');

  constructor(private diceService: DiceService) {}

  ngOnInit() {
    this.loadPresets();
    this.loadRollHistory();
  }

  get selectedPreset() {
    return this.presets().find(preset => preset.id === this.selectedPresetId) ?? null;
  }

  get selectedPresetStillMatchesSettings() {
    const preset = this.selectedPreset;

    if (!preset) {
      return false;
    }

    return preset.diceType === this.diceType
      && preset.diceCount === this.diceCount
      && (preset.successThreshold ?? null) === this.successThreshold;
  }

  get diceSides() {
    return Number(this.diceType.replace('D', ''));
  }

  get thresholdOptions() {
    return Array.from({ length: this.diceSides }, (_, index) => index + 1);
  }

  get canRerollOnes() {
    return this.lastRoll()?.results.includes(1) ?? false;
  }

  get diceCountWarning() {
    if (!this.diceCount) {
      return 'Dice count is required';
    }

    if (this.diceCount < 1) {
      return 'You must roll at least 1 die';
    }

    if (this.diceCount > 100) {
      return 'You can roll a maximum of 100 dice at once';
    }

    return '';
  }

  get successThresholdWarning() {
    if (this.successThreshold === null || this.successThreshold === undefined) {
      return '';
    }

    if (this.successThreshold < 1 || this.successThreshold > this.diceSides) {
      return `Success threshold must be between 1 and ${this.diceSides} for ${this.diceType}`;
    }

    return '';
  }

  get presetNameWarning() {
    if (!this.presetName.trim()) {
      return 'Preset name is required';
    }

    if (this.presetName.trim().length > 80) {
      return 'Preset name may not be longer than 80 characters';
    }

    return '';
  }

  rollDice() {
    this.clearMessages();

    if (this.diceCountWarning) {
      this.errorMessage.set(this.diceCountWarning);
      return;
    }

    if (this.successThresholdWarning) {
      this.errorMessage.set(this.successThresholdWarning);
      return;
    }

    this.diceService.roll({
      diceType: this.diceType,
      diceCount: this.diceCount,
      successThreshold: this.successThreshold,
      presetId: this.selectedPresetStillMatchesSettings ? this.selectedPresetId : null
    }).subscribe({
      next: roll => this.applyRoll(roll, `Rolled ${roll.diceCount} ${roll.diceType} dice.`),
      error: error => this.handleError(error, 'Could not roll dice')
    });
  }

  rerollLastRoll() {
    this.clearMessages();

    this.diceService.rerollLast().subscribe({
      next: roll => this.applyRoll(roll, 'Rerolled the last roll with the same settings.'),
      error: error => this.handleError(error, 'Could not reroll the last roll')
    });
  }

  rerollAllOnes() {
    this.clearMessages();

    const roll = this.lastRoll();

    if (!roll) {
      this.errorMessage.set('Roll dice before trying to reroll specific dice.');
      return;
    }

    this.diceService.rerollValue({
      rollId: roll.id,
      rerollValue: 1
    }).subscribe({
      next: reroll => this.applyRoll(reroll, 'Rerolled all dice that showed a 1.'),
      error: error => this.handleError(error, 'Could not reroll dice with value 1')
    });
  }

  savePreset() {
    this.clearMessages();

    if (this.presetNameWarning) {
      this.errorMessage.set(this.presetNameWarning);
      return;
    }

    if (this.diceCountWarning) {
      this.errorMessage.set(this.diceCountWarning);
      return;
    }

    if (this.successThresholdWarning) {
      this.errorMessage.set(this.successThresholdWarning);
      return;
    }

    this.diceService.createPreset({
      name: this.presetName.trim(),
      diceType: this.diceType,
      diceCount: this.diceCount,
      successThreshold: this.successThreshold,
      phase: this.presetPhase.trim() || null
    }).subscribe({
      next: preset => {
        this.presets.update(presets => [...presets, preset].sort((a, b) => a.name.localeCompare(b.name)));
        this.selectedPresetId = preset.id;
        this.successMessage.set('Preset saved.');
      },
      error: error => this.handleError(error, 'Could not save preset')
    });
  }

  updateSelectedPreset() {
    this.clearMessages();

    if (!this.selectedPresetId) {
      this.errorMessage.set('Select a preset before updating it.');
      return;
    }

    if (this.presetNameWarning) {
      this.errorMessage.set(this.presetNameWarning);
      return;
    }

    if (this.diceCountWarning) {
      this.errorMessage.set(this.diceCountWarning);
      return;
    }

    if (this.successThresholdWarning) {
      this.errorMessage.set(this.successThresholdWarning);
      return;
    }

    this.diceService.updatePreset(this.selectedPresetId, {
      name: this.presetName.trim(),
      diceType: this.diceType,
      diceCount: this.diceCount,
      successThreshold: this.successThreshold,
      phase: this.presetPhase.trim() || null
    }).subscribe({
      next: updatedPreset => {
        this.presets.update(presets => presets
          .map(preset => preset.id === updatedPreset.id ? updatedPreset : preset)
          .sort((a, b) => a.name.localeCompare(b.name))
        );
        this.successMessage.set('Preset updated.');
      },
      error: error => this.handleError(error, 'Could not update preset')
    });
  }

  deletePreset(preset: DicePreset) {
    this.clearMessages();

    this.diceService.deletePreset(preset.id).subscribe({
      next: () => {
        this.presets.update(presets => presets.filter(existingPreset => existingPreset.id !== preset.id));

        if (this.selectedPresetId === preset.id) {
          this.clearPresetSelection();
        }

        this.successMessage.set('Preset deleted.');
      },
      error: error => this.handleError(error, 'Could not delete preset')
    });
  }

  usePreset(preset: DicePreset) {
    this.selectedPresetId = preset.id;
    this.presetName = preset.name;
    this.diceType = preset.diceType;
    this.diceCount = preset.diceCount;
    this.successThreshold = preset.successThreshold ?? null;
    this.presetPhase = preset.phase ?? '';
    this.clearMessages();
  }

  clearPresetSelection() {
    this.selectedPresetId = null;
    this.presetName = '';
    this.presetPhase = '';
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

  private applyRoll(roll: DiceRoll, message: string) {
    this.lastRoll.set(roll);
    this.diceType = roll.diceType;
    this.diceCount = roll.diceCount;
    this.successThreshold = roll.successThreshold ?? null;
    this.rollHistory.update(history => [roll, ...history].slice(0, 20));
    this.successMessage.set(message);
  }

  private loadPresets() {
    this.diceService.getPresets().subscribe({
      next: presets => this.presets.set(presets),
      error: error => this.handleError(error, 'Could not load presets')
    });
  }

  private loadRollHistory() {
    this.diceService.getRollHistory().subscribe({
      next: history => {
        this.rollHistory.set(history);
        this.lastRoll.set(history[0] ?? null);
      },
      error: error => this.handleError(error, 'Could not load roll history')
    });
  }

  private clearMessages() {
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  private handleError(error: HttpErrorResponse, fallbackMessage: string) {
    const response = error.error as Record<string, string> | string | null;

    if (response && typeof response === 'object') {
      this.errorMessage.set(response['name']
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
