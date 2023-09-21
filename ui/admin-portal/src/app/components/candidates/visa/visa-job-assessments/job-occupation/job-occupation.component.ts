import {Component, Input, OnInit} from '@angular/core';
import {Occupation} from '../../../../../model/occupation';
import {FormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-job-occupation',
  templateUrl: './job-occupation.component.html',
  styleUrls: ['./job-occupation.component.scss']
})
export class JobOccupationComponent extends VisaCheckComponentBase implements OnInit {

  @Input() occupations: Occupation[];

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobOccupationId: [this.visaJobCheck?.occupation?.id],
      visaJobOccupationNotes: [this.visaJobCheck?.occupationNotes],
    });

    this.form.controls['visaJobOccupationId']?.valueChanges.subscribe(
      change => {
        //Update my existingRecord with occupation object
          this.visaJobCheck.occupation =
            {id: change, name: null, isco08Code: null, status: null};
      }
    );
  }

  get occupationId(): number {
    return this.visaJobCheck?.occupation?.id;
  }

}
