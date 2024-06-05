import {Component, Input, OnInit} from '@angular/core';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";
import {CandidateDependant,} from "../../../../../model/candidate";
import {CandidateVisaJobService} from "../../../../../services/candidate-visa-job.service";

@Component({
  selector: 'app-relocating-dependants',
  templateUrl: './relocating-dependants.component.html',
  styleUrls: ['./relocating-dependants.component.scss']
})
export class RelocatingDependantsComponent extends VisaCheckComponentBase implements OnInit {

  @Input() dependants: CandidateDependant[];
  loading: boolean;

  constructor(fb: FormBuilder,
              candidateVisaCheckService: CandidateVisaCheckService,
              private candidateVisaJobService: CandidateVisaJobService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobRelocatingDependantIds: [this.visaJobCheck?.relocatingDependantIds],
    });
  }

  requestSfCaseRelocationInfoUpdate() {
    this.error = null;
    this.loading = true;
    this.candidateVisaJobService.updateSfCaseRelocationInfo(
      this.visaJobCheck.id).subscribe(
      boolean => {
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });
  }
}
