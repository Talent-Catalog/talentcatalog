import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck
} from "../../../../../../../model/candidate";
import {NgbAccordion} from "@ng-bootstrap/ng-bootstrap";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {CandidateOccupationService} from "../../../../../../../services/candidate-occupation.service";
import {OccupationService} from "../../../../../../../services/occupation.service";

@Component({
  selector: 'app-visa-job-check-uk',
  templateUrl: './visa-job-check-uk.component.html',
  styleUrls: ['./visa-job-check-uk.component.scss']
})
export class VisaJobCheckUkComponent implements OnInit, AfterViewInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  @ViewChild('visaJobUk') visaJobUk: NgbAccordion;

  error: string;

  constructor(private candidateEducationService: CandidateEducationService,
              private candidateOccupationService: CandidateOccupationService,
              private occupationService: OccupationService) {}

  ngOnInit() {
  }

  ngAfterViewInit() {
    this.visaJobUk.expandAll();
  }
}
