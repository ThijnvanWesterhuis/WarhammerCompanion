export type DiceType = 'D6' | 'D10' | 'D20';

export interface DicePreset {
  id: number;
  name: string;
  diceType: DiceType;
  diceCount: number;
  phase?: string | null;
  createdAt: string;
}

export interface DicePresetRequest {
  name: string;
  diceType: DiceType;
  diceCount: number;
  phase?: string | null;
}

export interface DiceRollRequest {
  diceType: DiceType;
  diceCount: number;
  presetId?: number | null;
}

export interface DiceRoll {
  id: number;
  diceType: DiceType;
  diceCount: number;
  results: number[];
  total: number;
  sourcePresetName?: string | null;
  createdAt: string;
}
