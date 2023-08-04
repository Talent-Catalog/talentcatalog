import {Component, Input, OnInit} from '@angular/core';
import {CandidateVisaJobCheck, getDestinationOccupationSubcatLink} from "../../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-occupation-subcategory',
  templateUrl: './occupation-subcategory.component.html',
  styleUrls: ['./occupation-subcategory.component.scss']
})
export class OccupationSubcategoryComponent extends IntakeComponentBase implements OnInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  occupationSubcatLink: string;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobOccupationSubcategory: [this.selectedJobCheck?.occupationSubcategory],
    });
    this.occupationSubcatLink = getDestinationOccupationSubcatLink(this.visaCheckRecord?.country.id);
  }

}
