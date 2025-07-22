/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {EducationLevel} from "../../../../../model/education-level";
import {EducationLevelService} from "../../../../../services/education-level.service";

@Component({
  selector: 'app-edit-candidate-max-education-level',
  templateUrl: './edit-candidate-max-education-level.html',
  styleUrls: ['./edit-candidate-max-education-level.scss']
})
export class EditMaxEducationLevelComponent implements OnInit {
  educationLevels: EducationLevel[] = [];
  selectedLevel: EducationLevel;
  currentLevel: EducationLevel;

  constructor(
    private educationLevelService: EducationLevelService,
    public activeModal: NgbActiveModal
  ) {}

  ngOnInit() {
    this.educationLevelService.listEducationLevels().subscribe(levels => {
      this.educationLevels = levels;
      this.selectedLevel = this.educationLevels.find(level => level.id === this.currentLevel?.id);
    });
  }

  save() {
    this.activeModal.close(this.selectedLevel);
  }
}
