import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateNoteService} from '../../../../../services/candidate-note.service';
import {CandidateNote} from '../../../../../model/candidate-note';
import {CountryService} from '../../../../../services/country.service';

@Component({
  selector: 'app-edit-candidate-note',
  templateUrl: './edit-candidate-note.component.html',
  styleUrls: ['./edit-candidate-note.component.scss']
})
export class EditCandidateNoteComponent implements OnInit {

  candidateNote: CandidateNote;

  candidateForm: FormGroup;

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

    // /*load the countries */
    // this.countryService.listCountries().subscribe(
    //   (response) => {
    //     this.countries = response;
    //   },
    //   (error) => {
    //     this.error = error;
    //     this.loading = false;
    //   }
    // );

    this.candidateForm = this.fb.group({
      title: [this.candidateNote.title],
      comment: [this.candidateNote.comment]
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    this.candidateNoteService.update(this.candidateNote.id, this.candidateForm.value).subscribe(
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
