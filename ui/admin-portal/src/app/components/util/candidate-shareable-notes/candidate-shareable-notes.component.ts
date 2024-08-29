import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {AutoSaveComponentBase} from "../autosave/AutoSaveComponentBase";
import {Candidate, UpdateCandidateShareableNotesRequest} from "../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {Observable} from "rxjs";
import {CandidateService} from "../../../services/candidate.service";

@Component({
  selector: 'app-candidate-shareable-notes',
  templateUrl: './candidate-shareable-notes.component.html',
  styleUrls: ['./candidate-shareable-notes.component.scss']
})
export class CandidateShareableNotesComponent extends AutoSaveComponentBase
  implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  constructor(private fb: FormBuilder, private candidateService: CandidateService) {
    super(candidateService);
  }

  ngOnInit() {
    this.form = this.fb.group({
      shareableNotes: [this.candidate.shareableNotes],
    });
  }

  doSave(formValue: any): Observable<Candidate> {
    const request: UpdateCandidateShareableNotesRequest = {
      shareableNotes: this.shareableNotes
    }
    return this.candidateService.updateShareableNotes(this.candidate.id, request);
  }

  onSuccessfulSave() {
    this.candidate.shareableNotes = this.shareableNotes;
  }

  get shareableNotes(): string {
    return this.form.value?.shareableNotes;
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Replace the form value with the new candidates data when
    //changing from one candidate to the next or when selection has changed.
    if (this.form) {
      if (this.editable) {
        this.form.controls['shareableNotes'].patchValue(this.candidate.shareableNotes);
      }
    }
  }
}
