export type DiceType = 'D6' | 'D10' | 'D20';

export interface DicePreset {
  id: number;
  name: string;
  diceType: DiceType;
  diceCount: number;
  successThreshold?: number | null;
  phase?: string | null;
  createdAt: string;
}

export interface DicePresetRequest {
  name: string;
  diceType: DiceType;
  diceCount: number;
  successThreshold?: number | null;
  phase?: string | null;
}

export interface DiceRollRequest {
  diceType: DiceType;
  diceCount: number;
  successThreshold?: number | null;
  presetId?: number | null;
}

export interface DiceRerollValueRequest {
  rollId: number;
  rerollValue: number;
}

export interface DiceRoll {
  id: number;
  diceType: DiceType;
  diceCount: number;
  results: number[];
  total: number;
  successThreshold?: number | null;
  successCount?: number | null;
  failCount?: number | null;
  sourcePresetName?: string | null;
  rerollSourceRollId?: number | null;
  rerollType?: string | null;
  createdAt: string;
}
