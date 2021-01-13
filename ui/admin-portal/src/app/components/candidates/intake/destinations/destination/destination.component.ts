import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateDestination, FamilyRelations, YesNoUnsureLearn} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';
import {Country} from '../../../../../model/country';

@Component({
  selector: 'app-destination',
  templateUrl: './destination.component.html',
  styleUrls: ['./destination.component.scss']
})
export class DestinationComponent extends IntakeComponentBase implements OnInit {
  @Input() country: Country;
  @Output() touched = new EventEmitter();

  public destAusOptions: EnumOption[] = enumOptions(YesNoUnsureLearn);
  public destAusFamilyOptions: EnumOption[] = enumOptions(FamilyRelations);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      destinationId: [this.myRecord?.id],
      destinationCountryId: [this.country.id],
      destinationInterest: [this.myRecord?.interest],
      destinationFamily: [this.myRecord?.family],
      destinationLocation: [this.myRecord?.location],
      destinationNotes: [this.myRecord?.notes],
    });
  }

  private get myRecord(): CandidateDestination {
    return this.candidateIntakeData.candidateDestinations ?
      this.candidateIntakeData.candidateDestinations[this.myRecordIndex]
      : null;
  }

  get interest(): string {
    return this.form?.value?.destinationInterest;
  }

  get family(): string {
    return this.form.value?.destinationFamily;
  }

  showLocation(): boolean {
    if (this.family === 'NoRelation' || this.family === null) {
      return false;
    } else {
      return true;
    }
  }

}
