import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateDependant, FamilyRelations} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {CandidateDependantService} from '../../../../../services/candidate-dependant.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';
import {NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-dependants-card',
  templateUrl: './dependants-card.component.html',
  styleUrls: ['./dependants-card.component.scss']
})
export class DependantsCardComponent extends IntakeComponentBase implements OnInit {

  @Output() delete = new EventEmitter();

  public maxDate: NgbDateStruct;
  public today: Date;

  //Drop down values for enumeration
  dependantRelations: EnumOption[] = enumOptions(FamilyRelations);

  constructor(fb: FormBuilder, candidateService: CandidateService,
              private candidateDependantService: CandidateDependantService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      dependantId: [this.myRecord?.id],
      dependantRelation: [this.myRecord?.relation],
      dependantDob: [this.myRecord?.dob],
      dependantHealth: [this.myRecord?.healthConcerns],
    });
    this.today = new Date();
    this.maxDate = {year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate()};
  }

  get hasSelectedNationality(): boolean {
    let found: boolean = false;
    if (this.form?.value) {
      found = this.form.value.citizenNationalityId;
    }
    return found;
  }

  private get myRecord(): CandidateDependant {
    return this.candidateIntakeData.candidateDependants ?
      this.candidateIntakeData.candidateDependants[this.myRecordIndex]
      : null;
  }

  doDelete() {
    this.candidateDependantService.delete(this.myRecord.id)
      .subscribe(
        ret => {
        },
        error => {
          this.error = error;
        }
      );
    this.delete.emit();
  }
}
