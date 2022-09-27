import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../../util/enum";
import {Candidate, Gender, UnhcrStatus, YesNo, YesNoUnsure} from "../../../../../model/candidate";
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {CountryService} from "../../../../../services/country.service";

@Component({
  selector: 'app-edit-candidate-registration',
  templateUrl: './edit-candidate-registration.component.html',
  styleUrls: ['./edit-candidate-registration.component.scss']
})
export class EditCandidateRegistrationComponent implements OnInit {

  public registeredOptions: EnumOption[] = enumOptions(YesNoUnsure);
  public unhcrConsentOptions: EnumOption[] = enumOptions(YesNo);
  public unhcrStatusOptions: EnumOption[] = enumOptions(UnhcrStatus);

  public genderOptions: EnumOption[] = enumOptions(Gender);

  candidateId: number;
  candidateForm: FormGroup;
  nationalities = [];
  countries = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateService: CandidateService,
              private countryService: CountryService ) {
  }

  ngOnInit() {
    this.loading = true;

    this.candidateService.get(this.candidateId).subscribe(candidate => {
      this.candidateForm = this.fb.group({
        externalId: [candidate.externalId ? candidate.externalId : null],
        externalIdSource: [candidate.externalIdSource ? candidate.externalIdSource : null],
        partnerRef: [candidate.partnerRef ? candidate.partnerRef : null],
        unhcrNumber: [candidate.unhcrNumber ? candidate.unhcrNumber : null],
        unhcrStatus: [candidate.unhcrStatus ? candidate.unhcrStatus : null],
        unhcrConsent: [candidate.unhcrConsent ? candidate.unhcrConsent : null],
        unrwaRegistered: [candidate.unrwaRegistered ? candidate.unrwaRegistered : null],
        unrwaNumber: [candidate.unrwaNumber ? candidate.unrwaNumber : null],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.candidateService.updateRegistration(this.candidateId, this.candidateForm.value).subscribe(
      (candidate) => {
        this.closeModal(candidate);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidate: Candidate) {
    this.activeModal.close(candidate);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
