import {Component, Input, OnInit} from '@angular/core';
import {CandidateService} from "../../../../../services/candidate.service";
import {NationalityService} from "../../../../../services/nationality.service";
import {IntakeComponentTabBase} from "../../../../util/intake/IntakeComponentTabBase";
import {Candidate, CandidateIntakeData} from "../../../../../model/candidate";
import {FormBuilder, FormGroup} from "@angular/forms";

@Component({
  selector: 'app-candidate-visa-tab',
  templateUrl: './candidate-visa-tab.component.html',
  styleUrls: ['./candidate-visa-tab.component.scss']
})
export class CandidateVisaTabComponent
  extends IntakeComponentTabBase implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  form: FormGroup;
  selectedIndex: number;

  constructor(candidateService: CandidateService,
              nationalityService: NationalityService,
              private fb: FormBuilder) {
    super(candidateService, nationalityService)
  }

  ngOnInit() {
    super.ngOnInit();

    //If we have some visa checks, select the first one
    if (this.candidateIntakeData?.candidateVisaChecks.length > 0) {
      this.selectedIndex = 0;
    }
    this.form = this.fb.group({
      visaCountry: [this.selectedIndex]
    });
  }

  addRecord() {
    //todo
    // this.saving = true;
    // const candidateCitizenship: CandidateCitizenship = {};
    // this.candidateCitizenshipService.create(this.candidate.id, candidateCitizenship).subscribe(
    //   (citizenship) => {
    //     this.candidateIntakeData.candidateCitizenships.push(citizenship)
    //     this.saving = false;
    //   },
    //   (error) => {
    //     this.error = error;
    //     this.saving = false;
    //   });
  }

  deleteRecord(i: number) {
    this.candidateIntakeData.candidateVisaChecks.splice(i, 1);
  }

  changeVisaCountry(event: Event) {
    this.selectedIndex = this.form.controls.visaCountry.value;
    this.refreshIntakeData();
  }

}
