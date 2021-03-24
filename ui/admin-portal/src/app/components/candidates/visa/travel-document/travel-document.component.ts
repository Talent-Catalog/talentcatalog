import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {CandidateVisa, TravelDocumentStatus} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-travel-document',
  templateUrl: './travel-document.component.html',
  styleUrls: ['./travel-document.component.scss']
})
export class TravelDocumentComponent extends IntakeComponentBase implements OnInit {

//Drop down values for enumeration
  travelDocumentOptions: EnumOption[] = enumOptions(TravelDocumentStatus);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.myRecord?.id],
      visaCountryId: [this.myRecord?.country?.id],
      travelDocument: [this.myRecord?.eligibility],
      visaAssessmentNotes: [this.myRecord?.assessmentNotes],
    });
  }

  private get myRecord(): CandidateVisa {
    return this.candidateIntakeData?.candidateVisaChecks ?
      this.candidateIntakeData.candidateVisaChecks[this.myRecordIndex]
      : null;
  }

}
