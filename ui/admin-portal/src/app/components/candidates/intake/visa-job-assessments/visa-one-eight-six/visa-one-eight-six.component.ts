import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-visa-one-eight-six',
  templateUrl: './visa-one-eight-six.component.html',
  styleUrls: ['./visa-one-eight-six.component.scss']
})
export class VisaOneEightSixComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedIndex: number;
  public visa186Options: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visa186: [null],
    });
  }
}
