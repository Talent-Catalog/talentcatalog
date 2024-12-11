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

import { Component, OnInit } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { CandidateOccupationService } from "src/app/services/candidate-occupation.service";
import { CandidateService } from "src/app/services/candidate.service";

@Component({
  selector: "app-tailored-cv",
  templateUrl: "./tailored-cv.component.html",
  styleUrls: ["./tailored-cv.component.scss"],
})
export class TailoredCvComponent implements OnInit {
  loading: boolean;
  candidateId: number;
  candidateNumber: number;
  candidateOccupations: CandidateOccupationModel[];
  error;
  token: string;
  publicCvLink: string;

  constructor(
    private activeModal: NgbActiveModal,
    private candidateOccupationService: CandidateOccupationService,
    private candidateService: CandidateService
  ) {}


  ngOnInit(): void {
    this.loading = true;
    this.candidateOccupations = [];
    this.candidateOccupationService
      .get(this.candidateId)
      .subscribe(
        (results) => {
          this.candidateOccupations = results.map((x) => {
            return new CandidateOccupationModel(
              x.id,
              x.occupation.name,
              x.yearsExperience,
              true
            );
          });
          this.updateLink();
        },
        (error) => {
          this.error = error;
        }
      )
      .add(() => {
        this.loading = false;
      });
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
  selectAll(select:boolean) {
    this.candidateOccupations.forEach((x) => {
      x.selected = select;
    });
    this.updateLink();
  }

  selectItem(select:boolean, index:number) {
    this.candidateOccupations[index].selected = select;
    this.updateLink();
  }

  updateLink() {

    const candidateOccupationIds = this.candidateOccupations.
      filter(x => x.selected).
      map(x => x.id);

    //disable the link so it's not clicked before the API call returns the actual link
    this.publicCvLink = null;

    this.candidateService.
      generateToken(this.candidateNumber.toString(), true, candidateOccupationIds).
      subscribe(
        (results) => {
          this.token = results;
          this.publicCvLink = this.generatePublicCvLink();
        },
        (error) => {
          this.error = error;
        }
      )
  }

  generatePublicCvLink() {
    const isDevSetup = document.location.port == '4201';
    let origin = document.location.hostname;
    let path = '/public-portal/cv/';
    let protocol = document.location.protocol;
    if (isDevSetup) {
      origin = `${document.location.hostname}:4202`;
      path = '/cv/'
    }
    return `${protocol}//${origin}${path}${this.token}`;
  }

}

export class CandidateOccupationModel {
  constructor(
    public id: number,
    public occupation: string,
    public yearsExperience: number,
    public selected: boolean = false
  ) {}
}
