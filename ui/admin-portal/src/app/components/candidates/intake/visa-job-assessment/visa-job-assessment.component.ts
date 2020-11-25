import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData, CandidateVisaCheck} from "../../../../model/candidate";
import {Nationality} from "../../../../model/nationality";
import {Country} from "../../../../model/country";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateCandidateVisaCheckRequest} from "../../../../services/candidate-visa-check.service";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";
import {CreateVisaJobAssessementComponent} from "./modal/create-visa-job-assessement.component";

@Component({
  selector: 'app-visa-job-assessment',
  templateUrl: './visa-job-assessment.component.html',
  styleUrls: ['./visa-job-assessment.component.scss']
})
export class VisaJobAssessmentComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  error: boolean;
  loading: boolean;
  @Input() nationalities: Nationality[];
  saving: boolean;

  constructor(
    private modalService: NgbModal
  ) {}

  ngOnInit(): void {
  }

  addRecord() {
    const modal = this.modalService.open(CreateVisaJobAssessementComponent);
    modal.componentInstance.label = "TBB Job Opportunities";

    modal.result
      .then((selection: Country) => {
        if (selection) {
          this.createRecord(selection);
        }
      })
      .catch(() => {
        //User cancelled selection
      });
  }

  createRecord(country: Country) {
    this.loading = true;
    const request: CreateCandidateVisaCheckRequest = {
      countryId: country.id
    };
    this.candidateVisaCheckService.create(this.candidate.id, request)
      .subscribe(
        (visaCheck) => {
          this.candidateIntakeData.candidateVisaChecks.push(visaCheck)
          this.loading = false;
        },
        (error) => {
          this.error = error;
          this.loading = false;
        });

  }

  deleteRecord(i: number) {
    const confirmationModal = this.modalService.open(ConfirmationComponent);
    const visaCheck: CandidateVisaCheck = this.candidateIntakeData.candidateVisaChecks[i];

    confirmationModal.componentInstance.message =
      "Are you sure you want to delete the visa check for " + visaCheck.country.name;
    confirmationModal.result
      .then((result) => {
        if (result === true) {
          this.doDelete(i, visaCheck);
        }
      })
      .catch(() => {});
  }

  private doDelete(i: number, visaCheck: CandidateVisaCheck) {
    this.loading = true;
    this.candidateVisaCheckService.delete(visaCheck.id).subscribe(
      (done) => {
        this.loading = false;
        this.candidateIntakeData.candidateVisaChecks.splice(i, 1);
        this.changeVisaCountry(null);
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  changeVisaCountry(event: Event) {
    this.selectedIndex = this.form.controls.visaCountry.value;
    this.selectedCountry = this.candidateIntakeData
      .candidateVisaChecks[this.selectedIndex]?.country?.name;
  }

}
