import {Component, OnInit} from '@angular/core';
import {getDestinationOccupationSubcatLink} from "../../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-occupation-subcategory',
  templateUrl: './occupation-subcategory.component.html',
  styleUrls: ['./occupation-subcategory.component.scss']
})
export class OccupationSubcategoryComponent extends VisaCheckComponentBase implements OnInit {
  occupationSubcatLink: string;

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobOccupationSubCategory: [this.visaJobCheck?.occupationSubCategory],
    });
    this.occupationSubcatLink = getDestinationOccupationSubcatLink(this.visaCheck?.country.id);
  }

}
