/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {CandidateSkill} from "../../../../model/candidate-skill";
import {CandidateSkillService} from "../../../../services/candidate-skill.service";

@Component({
  selector: 'app-view-candidate-skill',
  templateUrl: './view-candidate-skill.component.html',
  styleUrls: ['./view-candidate-skill.component.scss']
})
export class ViewCandidateSkillComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  candidateSkills: CandidateSkill[];
  loading: boolean;
  error;

  constructor(private candidateSkillService: CandidateSkillService) {
  }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {

  }

  doSearch() {
    this.loading = true;
    let request = {
      candidateId: this.candidate.id,
      pageNumber: 0,
      pageSize: 20
    };
    this.candidateSkillService.search(request).subscribe(
      candidateSkills => {
        this.candidateSkills = candidateSkills.content;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }


}
