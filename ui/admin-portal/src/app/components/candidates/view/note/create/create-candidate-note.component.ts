import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateNoteService} from "../../../../../services/candidate-note.service";
import {CandidateNote} from "../../../../../model/candidate-note";
import {CountryService} from "../../../../../services/country.service";

@Component({
  selector: 'app-create-candidate-note',
  templateUrl: './create-candidate-note.component.html',
  styleUrls: ['./create-candidate-note.component.scss']
})
export class CreateCandidateNoteComponent implements OnInit {

  candidateNote: CandidateNote;

  candidateForm: FormGroup;

  candidateId: number;
  countries = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateNoteService: CandidateNoteService,
              private countryService: CountryService ) {
  }

  ngOnInit() {
    this.loading = true;

    /* load the years */
    this.years = [];
    let currentYear = new Date().getFullYear();
    let year = 1950;
    while (year < currentYear){
      this.years.push(year++);
    }

    /*load the countries */
    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    this.candidateForm = this.fb.group({
      candidateId: [this.candidateId],
      title: ['', [Validators.required]],
      comment: ['', [Validators.required]]
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    this.candidateNoteService.create(this.candidateForm.value).subscribe(
      (candidateNote) => {
        this.closeModal(candidateNote);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateNote: CandidateNote) {
    this.activeModal.close(candidateNote);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
