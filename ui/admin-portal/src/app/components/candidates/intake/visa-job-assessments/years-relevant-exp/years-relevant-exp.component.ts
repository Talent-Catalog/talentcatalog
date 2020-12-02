import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-years-relevant-exp',
  templateUrl: './years-relevant-exp.component.html',
  styleUrls: ['./years-relevant-exp.component.scss']
})
export class YearsRelevantExpComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedIndex: number;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      yrsRelevantWorkExp: [null]
    });
  }

}
