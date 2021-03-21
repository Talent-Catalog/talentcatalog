import {Component, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-int-protection',
  templateUrl: './int-protection.component.html',
  styleUrls: ['./int-protection.component.scss']
})
export class IntProtectionComponent extends IntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheckRecord?.id],
      visaCountryId: [this.visaCheckRecord?.country.id],
      visaProtectionGrounds: [this.visaCheckRecord?.protectionGrounds],
    });
  }

}
