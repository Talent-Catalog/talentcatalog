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
import {CandidateJobExperience} from "../../../model/candidate-job-experience";
import {Country} from "../../../model/country";

@Component({
  selector: 'app-candidate-job-experience-card',
  templateUrl: './candidate-job-experience-card.component.html',
  styleUrls: ['./candidate-job-experience-card.component.scss']
})
export class CandidateJobExperienceCardComponent implements OnInit {

  @Input() preview: boolean = false;
  @Input() experience: CandidateJobExperience;
  @Input() countries: Country[];
  @Input() disabled: boolean;

  @Output() onEdit = new EventEmitter<CandidateJobExperience>();
  @Output() onDelete = new EventEmitter<CandidateJobExperience>();

  constructor() { }

  ngOnInit() {

  }

  edit() {
    this.onEdit.emit(this.experience);
  }

  delete() {
    this.onDelete.emit(this.experience);
  }

  getCountryName(country: Country) {
    return this.countries?.find(c => c.id === country.id)?.name;
  }

  isHtml(text) {
    // Very simple test for HTML tags - isn't not foolproof but probably good enough
    return /<\/?[a-z][\s\S]*>/i.test(text);
  }

}
