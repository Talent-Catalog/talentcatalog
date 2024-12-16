/*
 * Copyright (c) 2024 Talent Catalog.
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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateEducation} from "../../../model/candidate-education";
import {Country} from "../../../model/country";
import {EducationMajor} from "../../../model/education-major";

@Component({
  selector: 'app-candidate-education-card',
  templateUrl: './candidate-education-card.component.html',
  styleUrls: ['./candidate-education-card.component.scss']
})
export class CandidateEducationCardComponent implements OnInit {

  @Input() preview: boolean = false;
  @Input() disabled: boolean = false;
  @Input() candidateEducation: CandidateEducation;
  @Input() countries: Country[];
  @Input() majors: EducationMajor[];

  @Output() onDelete = new EventEmitter();
  @Output() onEdit = new EventEmitter<CandidateEducation>();

  constructor() { }

  ngOnInit() {
  }

  delete() {
    this.onDelete.emit()
  }

  edit() {
    this.onEdit.emit(this.candidateEducation);
  }

  getCountryName(country: Country) {
    return this.countries?.find(c => c.id === country.id)?.name;
  }

  getMajorName(major: EducationMajor) {
    return this.majors?.find(m => m.id === major.id)?.name;
  }
}
