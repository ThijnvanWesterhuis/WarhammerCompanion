import { PageResponse } from '../../session/models/session.models';

export interface GameEdition {
  id: number;
  code: string;
  displayName: string;
  description?: string | null;
  releaseOrder: number;
}

export interface UnitCatalogItem {
  id: number;
  editionCode: string;
  faction: string;
  name: string;
  unitType: string;
  points: number;
  models?: string | null;
  keywords?: string | null;
}

export interface ArmyListUnitRequest {
  name: string;
  unitType?: string | null;
  points: number;
  quantity: number;
  notes?: string | null;
}

export interface ArmyListRequest {
  name: string;
  gameEditionCode: string;
  faction: string;
  armyRule?: string | null;
  pointsLimit: number;
  description?: string | null;
  units: ArmyListUnitRequest[];
}

export interface ArmyListUnit extends ArmyListUnitRequest {
  id: number;
  totalPoints: number;
}

export interface ArmyList {
  id: number;
  gameEdition: GameEdition;
  name: string;
  faction: string;
  armyRule?: string | null;
  pointsLimit: number;
  totalPoints: number;
  remainingPoints: number;
  description?: string | null;
  units: ArmyListUnit[];
  createdAt: string;
  updatedAt: string;
}

export type ArmyListPage = PageResponse<ArmyList>;
