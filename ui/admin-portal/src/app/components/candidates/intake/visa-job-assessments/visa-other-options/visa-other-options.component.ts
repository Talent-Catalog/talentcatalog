import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {OtherVisas} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-visa-other-options',
  templateUrl: './visa-other-options.component.html',
  styleUrls: ['./visa-other-options.component.scss']
})
export class VisaOtherOptionsComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedIndex: number;
  public visaOtherOptions: EnumOption[] = enumOptions(OtherVisas);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaOther: [null],
    });
  }

}
