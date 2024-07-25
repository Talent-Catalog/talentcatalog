import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {CandidateDependant,} from "../../../../../model/candidate";
import {AutoSaveComponentBase} from "../../../../util/autosave/AutoSaveComponentBase";
import {CandidateOpportunity} from "../../../../../model/candidate-opportunity";
import {Observable} from "rxjs";
import {
  CandidateOpportunityService,
  UpdateRelocatingDependantIds
} from "../../../../../services/candidate-opportunity.service";
import {CandidateDependantService} from "../../../../../services/candidate-dependant.service";

@Component({
  selector: 'app-relocating-dependants',
  templateUrl: './relocating-dependants.component.html',
  styleUrls: ['./relocating-dependants.component.scss']
})
export class RelocatingDependantsComponent extends AutoSaveComponentBase implements OnInit {

  @Input() candidateOpp: CandidateOpportunity;
  @Input() candidateId: number;
  dependants: CandidateDependant[];
  loading: boolean;
  updatingSf: boolean;

  constructor(private fb: FormBuilder,
              private candidateOpportunityService: CandidateOpportunityService,
              private candidateDependantService: CandidateDependantService) {
    super(null);
  }

  ngOnInit(): void {
    this.fetchDependants()
    this.form = this.fb.group({
      relocatingDependantIds: [this.candidateOpp?.relocatingDependantIds],
    });
  }

  fetchDependants() {
    this.loading = true;
    this.candidateDependantService.list(this.candidateId).subscribe(
      (results) => {
        this.dependants = results;
        this.loading = false;
      }, (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  doSave(formValue: any): Observable<void> {
    const request: UpdateRelocatingDependantIds = {
      id: this.candidateOpp.id,
      relocatingDependantIds: this.form.value.relocatingDependantIds
    }
    return this.candidateOpportunityService.updateRelocatingDependants(this.candidateOpp.id, request);
  }

  onSuccessfulSave() {
    this.candidateOpp.relocatingDependantIds = this.form.value.relocatingDependantIds;
  }

  requestSfCaseRelocationInfoUpdate() {
    this.error = null;
    this.updatingSf = true;
    this.candidateOpportunityService.updateSfCaseRelocationInfo(
      this.candidateOpp.id).subscribe(
      boolean => {
        this.updatingSf = false;
      },
      error => {
        this.error = error;
        this.updatingSf = false;
      });
  }
}
