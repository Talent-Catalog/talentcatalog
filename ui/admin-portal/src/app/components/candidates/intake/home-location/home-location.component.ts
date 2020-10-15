import {Component, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-home-location',
  templateUrl: './home-location.component.html',
  styleUrls: ['./home-location.component.scss']
})
export class HomeLocationComponent extends IntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      homeLocation: [this.candidateIntakeData?.homeLocation],
    });
  }

}
