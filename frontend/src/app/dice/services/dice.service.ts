import { Injectable } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import {
  DicePreset,
  DicePresetRequest,
  DiceRoll,
  DiceRollRequest
} from '../models/dice.models';

@Injectable({ providedIn: 'root' })
export class DiceService {
  constructor(private api: ApiService) {}

  getPresets() {
    return this.api.get<DicePreset[]>('/dice/presets');
  }

  createPreset(request: DicePresetRequest) {
    return this.api.post<DicePreset>('/dice/presets', request);
  }

  updatePreset(id: number, request: DicePresetRequest) {
    return this.api.put<DicePreset>(`/dice/presets/${id}`, request);
  }

  deletePreset(id: number) {
    return this.api.delete<void>(`/dice/presets/${id}`);
  }

  roll(request: DiceRollRequest) {
    return this.api.post<DiceRoll>('/dice/roll', request);
  }

  getRollHistory() {
    return this.api.get<DiceRoll[]>('/dice/rolls');
  }
}
