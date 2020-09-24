import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Candidate} from '../../../model/candidate';
import {User} from '../../../model/user';
import {FormBuilder, FormGroup} from '@angular/forms';
import {catchError, debounceTime, switchMap, takeUntil} from 'rxjs/operators';

@Component({
  selector: 'app-candidate-search-card',
  templateUrl: './candidate-search-card.component.html',
  styleUrls: ['./candidate-search-card.component.scss']
})
export class CandidateSearchCardComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() loggedInUser: User;
  @Input() sourceType: string;
  @Input() sourceName: string;

  @Output() onClose = new EventEmitter();

  form: FormGroup;

  constructor(private fb: FormBuilder) { }

  ngOnInit() {
    this.form = this.fb.group({
      contextNotes: [''],
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      // TODO switch to general tab?
    }
  }

  private autoSaveNote(timeout: number) {
    this.form.valueChanges.pipe(

      //Only pass values on if there has been inactivity for the given timeout
      debounceTime(timeout),

      //Do a save of the received form values.
      switchMap(formValue => {
          //Update the candidateIntakeData to keep it in sync with the server
          //saved version.
          //Object assign just copies the formValue fields across, leaving any
          //other fields in candidateIntakeData unchanged.
          //Object.assign(this.candidateIntakeData, formValue);
          // this.error = null;
          // this.saving = true;
          console.log(formValue)
          //return this.candidateService.updateIntakeData(this.candidate.id, formValue);
        }
      ),

      //We catch errors, copying them to this.error, but then just continuing
      catchError((error, caught) => {
        // this.saving = false;
        // this.error = error;
        return caught;
      }),

      //Subscription will continue until the given Observable emits.
      //See ngOnDestroy
      takeUntil(this.unsubscribe)
    ).subscribe(

      //Save has completed successfully
      () => this.saving = false,

      //Theoretically never get here because we catch errors in the pipe
      (error) => {
        this.saving = false;
        this.error = error;
      }
    )
  }

  close() {
    this.onClose.emit();
  }

}
