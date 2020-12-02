import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-visa-four-nine-four',
  templateUrl: './visa-four-nine-four.component.html',
  styleUrls: ['./visa-four-nine-four.component.scss']
})
export class VisaFourNineFourComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedIndex: number;
  public visa494Options: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visa186: [null],
    });
  }
}
