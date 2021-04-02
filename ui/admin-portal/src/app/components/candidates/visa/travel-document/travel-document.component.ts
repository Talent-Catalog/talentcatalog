import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {TravelDocumentStatus} from "../../../../model/candidate";
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
      visaId: [this.visaCheckRecord?.id],
      visaCountryId: [this.visaCheckRecord?.country?.id],
      visaValidTravelDocs: [this.visaCheckRecord?.validTravelDocs],
      visaValidTravelDocsNotes: [this.visaCheckRecord?.validTravelDocsNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaValidTravelDocs) {
      if (this.form.value.visaValidTravelDocs === 'Valid') {
        found = true
      }
      if (this.form.value.visaValidTravelDocs === 'Expired') {
        found = true
      }
      if (this.form.value.visaValidTravelDocs === 'None') {
        found = true
      }
    }
    return found;
  }

}
