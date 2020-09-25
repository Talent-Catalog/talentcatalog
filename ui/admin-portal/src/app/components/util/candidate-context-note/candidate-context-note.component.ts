import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Candidate} from '../../../model/candidate';
import {debounceTime, switchMap} from 'rxjs/operators';
import {CandidateService} from '../../../services/candidate.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-candidate-context-note',
  templateUrl: './candidate-context-note.component.html',
  styleUrls: ['./candidate-context-note.component.scss']
})
export class CandidateContextNoteComponent implements OnInit, AfterViewInit {

  @Input() candidate: Candidate;
  @Input() sourceType: string;
  @Input() sourceName: string;

  data: Observable<any>;

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService) { }

  ngOnInit() {
    this.form = this.fb.group({
      contextNotes: [''],
    });
  }

  ngAfterViewInit() {
    //3 second timeout
    this.form.valueChanges.pipe(debounceTime(3000), switchMap(formValue => (debounceTime(10)))).subscribe(formValue => {
      console.log(formValue)
    })
  }

  // private autoSaveNote(timeout: number) {
  //   this.form.valueChanges.pipe(
  //
  //     //Only pass values on if there has been inactivity for the given timeout
  //     debounceTime(timeout),
  //
  //     //Do a save of the received form values.
  //     switchMap(formValue => {
  //         //Update the candidateIntakeData to keep it in sync with the server
  //         //saved version.
  //         //Object assign just copies the formValue fields across, leaving any
  //         //other fields in candidateIntakeData unchanged.
  //         //Object.assign(this.candidateIntakeData, formValue);
  //         // this.error = null;
  //         // this.saving = true;
  //         console.log(formValue)
  //         return formValue
  //         //return this.candidateService.updateIntakeData(this.candidate.id, formValue);
  //       }
  //     ),
  //
  //     //We catch errors, copying them to this.error, but then just continuing
  //     catchError((error, caught) => {
  //       // this.saving = false;
  //       // this.error = error;
  //       return caught;
  //     }),
  //
  //     //Subscription will continue until the given Observable emits.
  //     //See ngOnDestroy
  //     takeUntil(this.unsubscribe)
  //   ).subscribe(
  //
  //     //Save has completed successfully
  //     () => this.saving = false,
  //
  //     //Theoretically never get here because we catch errors in the pipe
  //     (error) => {
  //       this.saving = false;
  //       this.error = error;
  //     }
  //   )
  // }

}
