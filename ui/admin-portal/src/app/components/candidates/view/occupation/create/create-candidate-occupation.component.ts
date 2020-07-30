import { Component, OnInit } from '@angular/core';
import {CandidateOccupation} from "../../../../../model/candidate-occupation";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Occupation} from "../../../../../model/occupation";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOccupationService} from "../../../../../services/candidate-occupation.service";
import {OccupationService} from "../../../../../services/occupation.service";

@Component({
  selector: 'app-create-candidate-occupation',
  templateUrl: './create-candidate-occupation.component.html',
  styleUrls: ['./create-candidate-occupation.component.scss']
})
export class CreateCandidateOccupationComponent implements OnInit {

  candidateOccupation: CandidateOccupation;

  form: FormGroup;
  candidateId: number;
  occupations: Occupation[];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateOccupationService: CandidateOccupationService,
              private occupationService: OccupationService ) {
  }

  ngOnInit() {
    this.loading = true;
    this.form = this.fb.group({
      occupationId: [null, Validators.required],
      yearsExperience: [null, [Validators.required, Validators.min(0)]],
    });

    /* LOAD OCCUPATIONS */
    this.occupationService.listOccupations().subscribe(
      (response) => {
        this.occupations = response;
        // console.log(this.occupations);
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  onSave() {
    this.saving = true;
    this.candidateOccupationService.create(this.candidateId, this.form.value).subscribe(
      (candidateOccupation) => {
        this.closeModal(candidateOccupation);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateOccupation: CandidateOccupation) {
    this.activeModal.close(candidateOccupation);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
