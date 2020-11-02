import {Component, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-host-challenges',
  templateUrl: './host-challenges.component.html',
  styleUrls: ['./host-challenges.component.scss']
})
export class HostChallengesComponent extends IntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      hostChallenges: [this.candidateIntakeData?.hostChallenges],
    });
  }

}
