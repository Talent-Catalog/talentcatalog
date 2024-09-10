import {ComponentFixture, TestBed} from '@angular/core/testing';
import {VisaJobNotesComponent} from './visa-job-notes.component';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidateVisaJobCheck} from "../../../../../MockData/MockCandidateVisaCheck";

describe('VisaJobNotesComponent', () => {
  let component: VisaJobNotesComponent;
  let fixture: ComponentFixture<VisaJobNotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VisaJobNotesComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [FormBuilder] // Provide the FormBuilder
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobNotesComponent);
    component = fixture.componentInstance;
    component.visaJobCheck = MockCandidateVisaJobCheck;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display error message if error is set', () => {
    component.error = 'Some error occurred';
    fixture.detectChanges();
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain('Some error occurred');
  });

  it('should set form control with provided notes', () => {
    const notes = 'Mock job notes';
    component.ngOnInit();
    const textarea: HTMLTextAreaElement = fixture.nativeElement.querySelector('textarea');
    expect(textarea.value).toBe(notes);
  });
});
