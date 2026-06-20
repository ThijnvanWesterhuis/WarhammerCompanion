import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  ArmyList,
  ArmyListRequest,
  ArmyListUnitRequest,
  GameEdition,
  UnitCatalogItem
} from '../../models/army-builder.models';
import { ArmyBuilderService } from '../../services/army-builder.service';
import { WARHAMMER_40K_FACTIONS } from '../../../shared/warhammer-factions';

@Component({
  selector: 'app-army-builder',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './army-builder.html',
  styleUrl: './army-builder.css'
})
export class ArmyBuilder implements OnInit {
  protected readonly factions = WARHAMMER_40K_FACTIONS;
  protected readonly catalogFactions = ['Ultramarines', 'Tyranids'];
  protected readonly pointsLimitOptions = [500, 1000, 2000];

  private readonly catalogFactionSet = new Set(this.catalogFactions);

  private readonly ninthEditionDetachmentOptions = [
    'Patrol Detachment',
    'Battalion Detachment',
    'Brigade Detachment',
    'Vanguard Detachment',
    'Spearhead Detachment',
    'Outrider Detachment',
    'Super-heavy Auxiliary Detachment'
  ];

  private readonly tenthEditionDetachmentOptionsByFaction: Record<string, string[]> = {
    Ultramarines: ['Gladius Task Force', 'Anvil Siege Force', 'Vanguard Spearhead'],
    Tyranids: ['Invasion Fleet', 'Synaptic Nexus', 'Vanguard Onslaught']
  };

  editions = signal<GameEdition[]>([]);
  armyLists = signal<ArmyList[]>([]);
  unitCatalog = signal<UnitCatalogItem[]>([]);
  selectedArmyList = signal<ArmyList | null>(null);

  loading = signal(false);
  loadingCatalog = signal(false);
  saving = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  page = signal(0);
  totalPages = signal(0);

  search = '';
  factionFilter = '';
  editionFilter = '';

  selectedCatalogUnitId = '';
  catalogSearch = '';

  form: ArmyListRequest = this.createEmptyForm();

  constructor(private armyBuilderService: ArmyBuilderService) {}

  ngOnInit() {
    this.loadEditions();
    this.loadArmyLists();
  }

  get isEditing() {
    return this.selectedArmyList() !== null;
  }

  get totalPoints() {
    return this.form.units.reduce((sum, unit) => sum + this.getUnitTotal(unit), 0);
  }

  get remainingPoints() {
    return (Number(this.form.pointsLimit) || 0) - this.totalPoints;
  }

  get selectedCatalogUnit() {
    return this.unitCatalog().find(unit => unit.id.toString() === this.selectedCatalogUnitId) ?? null;
  }

  get canLoadCatalog() {
    return Boolean(
      this.form.gameEditionCode &&
      this.form.faction &&
      this.catalogFactionSet.has(this.form.faction)
    );
  }

  get armyRuleOptions() {
    if (this.form.gameEditionCode === '9TH') {
      return this.ninthEditionDetachmentOptions;
    }

    return this.tenthEditionDetachmentOptionsByFaction[this.form.faction] ?? ['Index Detachment'];
  }

  get armyRuleLabel() {
    return this.form.gameEditionCode === '9TH'
      ? '9th Edition Detachment'
      : '10th Edition Detachment';
  }

  get armyRuleHelpText() {
    if (this.form.gameEditionCode === '9TH') {
      return '9th edition validates HQ, Troops, Elites, Fast Attack, Heavy Support, Flyer and Lord of War slots.';
    }

    return '10th edition validates Character requirement, Epic Hero uniqueness and datasheet limits.';
  }

  get catalogMessage() {
    if (!this.form.faction) {
      return 'Select a faction to load available units.';
    }

    if (!this.catalogFactionSet.has(this.form.faction)) {
      return 'For now the unit catalog only contains Ultramarines and Tyranids.';
    }

    if (this.loadingCatalog()) {
      return 'Loading unit catalog...';
    }

    if (this.unitCatalog().length === 0) {
      return 'No units found for this edition and faction.';
    }

    return '';
  }

  getUnitTotal(unit: ArmyListUnitRequest) {
    const points = Number(unit.points) || 0;
    const quantity = Number(unit.quantity) || 1;

    return points * quantity;
  }

  loadEditions() {
    this.armyBuilderService.getEditions().subscribe({
      next: editions => {
        this.editions.set(editions);

        if (!this.form.gameEditionCode && editions.length > 0) {
          this.form.gameEditionCode = editions[0].code;
        }

        this.ensureValidArmyRule();
        this.loadUnitCatalog();
      },
      error: error => this.handleError(error, 'Could not load editions')
    });
  }

  loadArmyLists(page = 0) {
    this.loading.set(true);
    this.errorMessage.set('');

    this.armyBuilderService
      .getArmyLists(page, 10, this.search, this.factionFilter, this.editionFilter)
      .subscribe({
        next: response => {
          this.armyLists.set(response.content);
          this.page.set(response.page);
          this.totalPages.set(response.totalPages);
          this.loading.set(false);
        },
        error: error => {
          this.loading.set(false);
          this.handleError(error, 'Could not load army lists');
        }
      });
  }

  loadUnitCatalog() {
    this.selectedCatalogUnitId = '';
    this.catalogSearch = '';

    if (!this.canLoadCatalog) {
      this.unitCatalog.set([]);
      return;
    }

    this.loadingCatalog.set(true);

    this.armyBuilderService.getUnitCatalog(this.form.gameEditionCode, this.form.faction).subscribe({
      next: units => {
        this.unitCatalog.set(units);
        this.loadingCatalog.set(false);
      },
      error: error => {
        this.unitCatalog.set([]);
        this.loadingCatalog.set(false);
        this.handleError(error, 'Could not load unit catalog');
      }
    });
  }

  onEditionOrFactionChanged() {
    this.ensureValidArmyRule();
    this.form.units = [];
    this.loadUnitCatalog();
  }

  filteredCatalogUnits() {
    const searchTerm = this.catalogSearch.trim().toLowerCase();

    if (!searchTerm) {
      return this.unitCatalog();
    }

    return this.unitCatalog().filter(unit =>
      unit.name.toLowerCase().includes(searchTerm) ||
      unit.unitType.toLowerCase().includes(searchTerm) ||
      (unit.keywords ?? '').toLowerCase().includes(searchTerm)
    );
  }

  applyFilters() {
    this.loadArmyLists(0);
  }

  clearFilters() {
    this.search = '';
    this.factionFilter = '';
    this.editionFilter = '';
    this.loadArmyLists(0);
  }

  previousPage() {
    if (this.page() > 0) {
      this.loadArmyLists(this.page() - 1);
    }
  }

  nextPage() {
    if (this.page() + 1 < this.totalPages()) {
      this.loadArmyLists(this.page() + 1);
    }
  }

  addSelectedCatalogUnit() {
    const selectedUnit = this.selectedCatalogUnit;

    if (!selectedUnit) {
      this.errorMessage.set('Select a unit from the catalog first');
      return;
    }

    const existingUnit = this.form.units.find(unit =>
      unit.name === selectedUnit.name &&
      unit.unitType === selectedUnit.unitType &&
      Number(unit.points) === Number(selectedUnit.points)
    );

    if (existingUnit) {
      existingUnit.quantity = (Number(existingUnit.quantity) || 1) + 1;
      this.successMessage.set(`${selectedUnit.name} quantity increased`);
    } else {
      this.form.units.push({
        name: selectedUnit.name,
        unitType: selectedUnit.unitType,
        points: selectedUnit.points,
        quantity: 1,
        notes: this.buildCatalogUnitNotes(selectedUnit)
      });

      this.successMessage.set(`${selectedUnit.name} added to army list`);
    }

    this.selectedCatalogUnitId = '';
  }

  addCustomUnit() {
    this.form.units.push(this.createEmptyUnit());
  }

  removeUnit(index: number) {
    this.form.units.splice(index, 1);
  }

  selectArmyList(armyList: ArmyList) {
    this.selectedArmyList.set(armyList);
    this.successMessage.set('');
    this.errorMessage.set('');
    this.form = this.toForm(armyList);
    this.ensureValidArmyRule();
    this.loadUnitCatalog();
  }

  resetForm() {
    this.selectedArmyList.set(null);
    this.form = this.createEmptyForm();

    const firstEdition = this.editions()[0];

    if (firstEdition) {
      this.form.gameEditionCode = firstEdition.code;
    }

    this.ensureValidArmyRule();
    this.errorMessage.set('');
    this.successMessage.set('');
    this.loadUnitCatalog();
  }

  saveArmyList() {
    this.saving.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const request = this.normalizeForm();
    const selected = this.selectedArmyList();

    const action = selected
      ? this.armyBuilderService.updateArmyList(selected.id, request)
      : this.armyBuilderService.createArmyList(request);

    action.subscribe({
      next: savedArmyList => {
        this.saving.set(false);
        this.successMessage.set(selected ? 'Army list updated' : 'Army list created');
        this.selectedArmyList.set(savedArmyList);
        this.form = this.toForm(savedArmyList);
        this.loadArmyLists(this.page());
        this.loadUnitCatalog();
      },
      error: error => {
        this.saving.set(false);
        this.handleError(error, 'Could not save army list');
      }
    });
  }

  duplicateArmyList(armyList: ArmyList) {
    this.armyBuilderService.duplicateArmyList(armyList.id).subscribe({
      next: duplicatedArmyList => {
        this.successMessage.set('Army list duplicated');
        this.selectArmyList(duplicatedArmyList);
        this.loadArmyLists(this.page());
      },
      error: error => this.handleError(error, 'Could not duplicate army list')
    });
  }

  deleteArmyList(armyList: ArmyList) {
    const confirmed = window.confirm(`Delete ${armyList.name}?`);

    if (!confirmed) {
      return;
    }

    this.armyBuilderService.deleteArmyList(armyList.id).subscribe({
      next: () => {
        this.successMessage.set('Army list deleted');

        if (this.selectedArmyList()?.id === armyList.id) {
          this.resetForm();
        }

        this.loadArmyLists(0);
      },
      error: error => this.handleError(error, 'Could not delete army list')
    });
  }

  private createEmptyForm(): ArmyListRequest {
    return {
      name: '',
      gameEditionCode: '10TH',
      faction: 'Ultramarines',
      armyRule: 'Gladius Task Force',
      pointsLimit: 2000,
      description: '',
      units: []
    };
  }

  private createEmptyUnit(): ArmyListUnitRequest {
    return {
      name: 'Custom Unit',
      unitType: this.form.gameEditionCode === '9TH' ? 'Troops' : 'Infantry',
      points: 0,
      quantity: 1,
      notes: ''
    };
  }

  private toForm(armyList: ArmyList): ArmyListRequest {
    return {
      name: armyList.name,
      gameEditionCode: armyList.gameEdition.code,
      faction: armyList.faction,
      armyRule: armyList.armyRule ?? '',
      pointsLimit: armyList.pointsLimit,
      description: armyList.description ?? '',
      units: armyList.units.map(unit => ({
        name: unit.name,
        unitType: unit.unitType ?? '',
        points: unit.points,
        quantity: unit.quantity,
        notes: unit.notes ?? ''
      }))
    };
  }

  private normalizeForm(): ArmyListRequest {
    return {
      name: this.form.name.trim(),
      gameEditionCode: this.form.gameEditionCode,
      faction: this.form.faction,
      armyRule: this.form.armyRule?.trim() || this.armyRuleOptions[0],
      pointsLimit: Number(this.form.pointsLimit) || 2000,
      description: this.form.description?.trim() || null,
      units: this.form.units
        .filter(unit => unit.name.trim())
        .map(unit => ({
          name: unit.name.trim(),
          unitType: unit.unitType?.trim() || null,
          points: Number(unit.points) || 0,
          quantity: Number(unit.quantity) || 1,
          notes: unit.notes?.trim() || null
        }))
    };
  }

  private ensureValidArmyRule() {
    const options = this.armyRuleOptions;

    if (!this.form.armyRule || !options.includes(this.form.armyRule)) {
      this.form.armyRule = options[0];
    }
  }

  private buildCatalogUnitNotes(unit: UnitCatalogItem) {
    const details = [unit.models, unit.keywords]
      .filter(value => value && value.trim())
      .join(' | ');

    return details || '';
  }

  private handleError(error: HttpErrorResponse, fallbackMessage: string) {
    const response = error.error as Record<string, string> | string | null;

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
