import { Injectable } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import {
  ArmyList,
  ArmyListPage,
  ArmyListRequest,
  GameEdition,
  UnitCatalogItem
} from '../models/army-builder.models';

@Injectable({ providedIn: 'root' })
export class ArmyBuilderService {
  constructor(private api: ApiService) {}

  getEditions() {
    return this.api.get<GameEdition[]>('/army-lists/editions');
  }

  getUnitCatalog(editionCode: string, faction: string) {
    const params = new URLSearchParams();
    params.set('editionCode', editionCode);
    params.set('faction', faction);

    return this.api.get<UnitCatalogItem[]>(`/unit-catalog?${params.toString()}`);
  }

  getArmyLists(page = 0, size = 10, search = '', faction = '', editionCode = '') {
    const params = new URLSearchParams();
    params.set('page', page.toString());
    params.set('size', size.toString());

    if (search.trim()) {
      params.set('search', search.trim());
    }

    if (faction.trim()) {
      params.set('faction', faction.trim());
    }

    if (editionCode.trim()) {
      params.set('editionCode', editionCode.trim());
    }

    return this.api.get<ArmyListPage>(`/army-lists?${params.toString()}`);
  }

  getArmyList(id: number) {
    return this.api.get<ArmyList>(`/army-lists/${id}`);
  }

  createArmyList(request: ArmyListRequest) {
    return this.api.post<ArmyList>('/army-lists', request);
  }

  updateArmyList(id: number, request: ArmyListRequest) {
    return this.api.put<ArmyList>(`/army-lists/${id}`, request);
  }

  duplicateArmyList(id: number) {
    return this.api.post<ArmyList>(`/army-lists/${id}/duplicate`, {});
  }

  deleteArmyList(id: number) {
    return this.api.delete<void>(`/army-lists/${id}`);
  }
}
