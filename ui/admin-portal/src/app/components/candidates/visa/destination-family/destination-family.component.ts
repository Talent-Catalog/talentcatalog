import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {FamilyRelations} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";

@Component({
  selector: 'app-destination-family',
  templateUrl: './destination-family.component.html',
  styleUrls: ['./destination-family.component.scss']
})
export class DestinationFamilyComponent extends VisaCheckComponentBase implements OnInit {

  public destFamilyOptions: EnumOption[] = enumOptions(FamilyRelations);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country?.id],
      visaDestinationFamily: [this.visaCheck?.destinationFamily],
      visaDestinationFamilyLocation: [this.visaCheck?.destinationFamilyLocation],
    });
  }

  get family(): string {
    return this.form.value?.visaDestinationFamily;
  }

  showLocation(): boolean {
    return !(this.family === 'NoRelation' || this.family === null);
  }

}
