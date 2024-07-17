import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {NgSelectModule} from '@ng-select/ng-select';
import {QualificationRelevantComponent} from './qualification-relevant.component';
import {CandidateVisaCheckService} from '../../../../../services/candidate-visa-check.service';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

fdescribe('QualificationRelevantComponent', () => {
  let component: QualificationRelevantComponent;
  let fixture: ComponentFixture<QualificationRelevantComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      declarations: [QualificationRelevantComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        FormBuilder,
        { provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy }
      ]
    }).compileComponents();

    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(QualificationRelevantComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // ngOnInit() is called here
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with visaJobQualification and visaJobQualificationNotes controls', () => {
    expect(component.form.contains('visaJobQualification')).toBeTrue();
    expect(component.form.contains('visaJobQualificationNotes')).toBeTrue();
  });

  it('should display error message when error is set', () => {
    component.error = 'Some error message';
    fixture.detectChanges();
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain('Some error message');
  });

  it('should initialize form controls with values from visaJobCheck', () => {
    component.visaJobCheck = { id: 123, qualification: YesNo.Yes, qualificationNotes: 'Relevant experience' };
    component.ngOnInit();
    expect(component.form.value.visaJobId).toBe(123);
    expect(component.form.value.visaJobQualification).toBe(YesNo.Yes);
    expect(component.form.value.visaJobQualificationNotes).toBe('Relevant experience');
  });

  it('should set relevantQualificationOptions correctly', () => {
    const expectedOptions: EnumOption[] = enumOptions(YesNo);
    expect(component.relevantQualificationOptions).toEqual(expectedOptions);
  });

  it('should render form elements when editable is true', () => {
    component.editable = true;
    fixture.detectChanges();
    const qualificationElement = fixture.nativeElement.querySelector('#visaJobQualification');
    const notesElement = fixture.nativeElement.querySelector('#visaJobQualificationNotes');
    expect(qualificationElement).toBeTruthy();
    expect(notesElement).toBeTruthy();
  });

  it('should not render form elements when editable is false', () => {
    component.editable = false;
    fixture.detectChanges();
    const qualificationElement = fixture.nativeElement.querySelector('#visaJobQualification');
    const notesElement = fixture.nativeElement.querySelector('#visaJobQualificationNotes');
    expect(qualificationElement).toBeFalsy();
    expect(notesElement).toBeFalsy();
  });
});
