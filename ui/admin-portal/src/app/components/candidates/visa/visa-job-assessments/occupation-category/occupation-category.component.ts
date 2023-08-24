import {Component, Input, OnInit} from '@angular/core';
import {CandidateVisaJobCheck, getDestinationOccupationCatLink} from "../../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-occupation-category',
  templateUrl: './occupation-category.component.html',
  styleUrls: ['./occupation-category.component.scss']
})
export class OccupationCategoryComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedJobCheck: CandidateVisaJobCheck;
  occupationCatLink: string;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobOccupationCategory: [this.selectedJobCheck?.occupationCategory],
    });

    this.occupationCatLink = getDestinationOccupationCatLink(this.visaCheckRecord?.country.id);
  }

}
