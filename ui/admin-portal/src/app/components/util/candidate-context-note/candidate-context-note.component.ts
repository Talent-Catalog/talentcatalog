import {AfterViewInit, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Candidate} from '../../../model/candidate';
import {catchError, debounceTime, switchMap, takeUntil} from 'rxjs/operators';
import {Observable, Subject} from 'rxjs';
import {CandidateSource, UpdateCandidateContextNoteRequest} from '../../../model/base';
import {CandidateSourceService} from '../../../services/candidate-source.service';

@Component({
  selector: 'app-candidate-context-note',
  templateUrl: './candidate-context-note.component.html',
  styleUrls: ['./candidate-context-note.component.scss']
})
export class CandidateContextNoteComponent implements OnInit, AfterViewInit, OnChanges, OnDestroy {

  @Input() candidate: Candidate;
  @Input() candidateSource: CandidateSource;

  data: Observable<any>;

  form: FormGroup;

  private unsubscribe = new Subject<void>()
  error;
  saving;

  constructor(private fb: FormBuilder,
              private candidateSourceService: CandidateSourceService) { }

  ngOnInit() {
    this.form = this.fb.group({
      contextNote: [this.candidate.contextNote],
    });
  }

  get contextNote(): string {
    return this.form.value?.contextNote;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes.candidate.firstChange) {
      if (changes.candidate.previousValue !== changes.candidate.currentValue) {
        this.form.controls['contextNote'].patchValue(this.candidate.contextNote);
      }
    }
  }

  ngAfterViewInit() {
    //3 second timeout
    this.autoSaveNote(3000);
  }

  private autoSaveNote(timeout: number) {
    this.form.valueChanges.pipe(

      //Only pass values on if there has been inactivity for the given timeout
      debounceTime(timeout),

      //Do a save of the received form values.
      switchMap(formValue => {

          const request: UpdateCandidateContextNoteRequest = {
            candidateId: this.candidate.id,
            contextNote: formValue.contextNote
          }
          this.error = null;
          this.saving = true;
          return this.candidateSourceService.updateContextNote(this.candidateSource, request);
        }
      ),

      //We catch errors, copying them to this.error, but then just continuing
      catchError((error, caught) => {
        this.saving = false;
        this.error = error;
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

  ngOnDestroy(): void {
    //Stop subscribing by emitting a value from the Unsubscribe Observable
    //See takeUntil in the above pipe.
    this.unsubscribe.next();
  }
}
