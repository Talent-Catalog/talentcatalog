import {Component, OnInit} from '@angular/core';
import {getDestinationOccupationCatLink} from "../../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-occupation-category',
  templateUrl: './occupation-category.component.html',
  styleUrls: ['./occupation-category.component.scss']
})
export class OccupationCategoryComponent extends VisaCheckComponentBase implements OnInit {

  occupationCatLink: string;

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobOccupationCategory: [this.visaJobCheck?.occupationCategory],
    });

    this.occupationCatLink = getDestinationOccupationCatLink(this.visaCheck?.country.id);
  }

}
