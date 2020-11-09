import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {DrivingLicenseStatus, YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-driving-license',
  templateUrl: './driving-license.component.html',
  styleUrls: ['./driving-license.component.scss']
})
export class DrivingLicenseComponent extends IntakeComponentBase implements OnInit {

  public canDriveOptions: EnumOption[] = enumOptions(YesNo);
  public drivingLicenseOptions: EnumOption[] = enumOptions(DrivingLicenseStatus);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      canDrive: [this.candidateIntakeData?.canDrive],
      drivingLicense: [this.candidateIntakeData?.drivingLicense],
      drivingLicenseExp: [this.candidateIntakeData?.drivingLicenseExp],
      drivingLicenseCountryId: [this.candidateIntakeData?.drivingLicenseCountry?.id],
    });
  }

  get canDrive(): string {
    return this.form.value?.canDrive;
  }

  get drivingLicense(): string {
    return this.form.value?.drivingLicense;
  }

  get hasDrivingLicense(): boolean {
    let found: boolean = false;
    if (this.form?.value) {
      if (this.drivingLicense === 'Other') {
        found = true;
      }
    }
    return found;
  }

}
