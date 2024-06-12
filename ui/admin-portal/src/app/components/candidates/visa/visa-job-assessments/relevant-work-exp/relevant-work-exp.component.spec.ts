import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule, FormBuilder} from '@angular/forms';
import {RelevantWorkExpComponent} from './relevant-work-exp.component';
import {CandidateVisaCheckService} from '../../../../../services/candidate-visa-check.service';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";

fdescribe('RelevantWorkExpComponent', () => {
  let component: RelevantWorkExpComponent;
  let fixture: ComponentFixture<RelevantWorkExpComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      declarations: [RelevantWorkExpComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [
        FormBuilder,
        { provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy }
      ]
    }).compileComponents();

    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RelevantWorkExpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // ngOnInit() is called here
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with visaJobRelevantWorkExp control', () => {
    expect(component.form.contains('visaJobRelevantWorkExp')).toBeTrue();
  });

  it('should display error message when error is set', () => {
    component.error = 'Some error message';
    fixture.detectChanges();
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain('Some error message');
  });

  it('should initialize form control with value from visaJobCheck', () => {
    component.visaJobCheck = { id: 123, relevantWorkExp: '2 years in last 5 years' };
    component.ngOnInit();
    expect(component.form.value.visaJobId).toBe(123);
    expect(component.form.value.visaJobRelevantWorkExp).toBe('2 years in last 5 years');
  });

  it('should render autosave status component', () => {
    const autosaveStatusComponent = fixture.nativeElement.querySelector('app-autosave-status');
    expect(autosaveStatusComponent).toBeTruthy();
  });

  it('should render textarea with correct placeholder', () => {
    const textareaElement: HTMLTextAreaElement = fixture.nativeElement.querySelector('#visaJobRelevantWorkExp');
    expect(textareaElement).toBeTruthy();
    expect(textareaElement.placeholder).toBe('');
  });

  it('should display the correct helper text', () => {
    const smallElement: HTMLElement = fixture.nativeElement.querySelector('small');
    expect(smallElement.textContent).toContain('Please include the date that this is recorded');
  });
});
