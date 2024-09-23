import {By} from '@angular/platform-browser';
import {CandidateShareableNotesComponent} from "./candidate-shareable-notes.component";
import {CandidateService} from "../../../services/candidate.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {Candidate, UpdateCandidateShareableNotesRequest} from "../../../model/candidate";
import {of} from "rxjs";
import {AutosaveStatusComponent} from "../autosave-status/autosave-status.component";

describe('CandidateShareableNotesComponent', () => {
  let component: CandidateShareableNotesComponent;
  let fixture: ComponentFixture<CandidateShareableNotesComponent>;
  let candidateService: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['updateShareableNotes']);

    await TestBed.configureTestingModule({
      declarations: [CandidateShareableNotesComponent,AutosaveStatusComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: CandidateService, useValue: candidateServiceSpy }
      ]
    }).compileComponents();

    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateShareableNotesComponent);
    component = fixture.componentInstance;
    component.candidate = { id: 1, shareableNotes: 'Initial Notes' } as Candidate;
    component.editable = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate shareable notes', () => {
    expect(component.form.value.shareableNotes).toBe('Initial Notes');
  });

  it('should update form value when candidate changes', () => {
    component.candidate = { id: 2, shareableNotes: 'Updated Notes' } as Candidate;
    component.ngOnChanges({});
    expect(component.form.value.shareableNotes).toBe('Updated Notes');
  });

  it('should call candidateService.updateShareableNotes on doSave', () => {
    const updatedNotes = 'Updated Notes';
    component.form.controls['shareableNotes'].setValue(updatedNotes);

    const request: UpdateCandidateShareableNotesRequest = { shareableNotes: updatedNotes };
    candidateService.updateShareableNotes.and.returnValue(of({ ...component.candidate, shareableNotes: updatedNotes }));

    component.doSave(component.form.value).subscribe(response => {
      expect(response.shareableNotes).toBe(updatedNotes);
    });

    expect(candidateService.updateShareableNotes).toHaveBeenCalledWith(1, request);
  });

  it('should update candidate shareableNotes on successful save', () => {
    const updatedNotes = 'Updated Notes';
    component.form.controls['shareableNotes'].setValue(updatedNotes);

    const request: UpdateCandidateShareableNotesRequest = { shareableNotes: updatedNotes };
    candidateService.updateShareableNotes.and.returnValue(of({ ...component.candidate, shareableNotes: updatedNotes }));

    component.doSave(component.form.value).subscribe(() => {
      component.onSuccessfulSave();
      expect(component.candidate.shareableNotes).toBe(updatedNotes);
    });
  });

  it('should disable textarea if not editable', () => {
    component.editable = false;
    fixture.detectChanges();

    const textarea = fixture.debugElement.query(By.css('textarea')).nativeElement;
    expect(textarea.readOnly).toBeTrue();
  });

  it('should enable textarea if editable', () => {
    component.editable = true;
    fixture.detectChanges();

    const textarea = fixture.debugElement.query(By.css('textarea')).nativeElement;
    expect(textarea.readOnly).toBeFalse();
  });

  it('should display error message if error is present', () => {
    component.error = 'Error message';
    fixture.detectChanges();

    const alert = fixture.debugElement.query(By.css('.alert-danger')).nativeElement;
    expect(alert.textContent).toContain('Error message');
  });

  it('should not display error message if error is not present', () => {
    component.error = null;
    fixture.detectChanges();

    const alert = fixture.debugElement.query(By.css('.alert-danger'));
    expect(alert).toBeNull();
  });
});
