import {Component, Input, OnInit} from '@angular/core';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';
import {CandidateExam} from "../../../../../model/candidate";

@Component({
  selector: 'app-ielts-level',
  templateUrl: './ielts-level.component.html',
  styleUrls: ['./ielts-level.component.scss']
})
export class IeltsLevelComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedIndex: number;

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      ieltsLevel: [null]
    });
  }

  get englishExams(): CandidateExam[] {
    return this.candidateIntakeData?.candidateExams;
  }
}
