import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-salary-tsmit',
  templateUrl: './salary-tsmit.component.html',
  styleUrls: ['./salary-tsmit.component.scss']
})
export class SalaryTsmitComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedIndex: number;
  public salaryRequirementOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      salaryTsmit: [null],
    });
  }

}
