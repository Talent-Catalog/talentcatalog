import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {SalaryTsmitComponent} from './salary-tsmit.component';
import {CandidateVisaCheckService} from '../../../../../services/candidate-visa-check.service';
import {By} from '@angular/platform-browser';
import {NgSelectModule} from '@ng-select/ng-select';
import {DebugElement} from '@angular/core';
import {YesNo} from '../../../../../model/candidate';
import {enumOptions} from '../../../../../util/enum';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

describe('SalaryTsmitComponent', () => {
  let component: SalaryTsmitComponent;
  let fixture: ComponentFixture<SalaryTsmitComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;
  let fb: FormBuilder;

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      declarations: [SalaryTsmitComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        FormBuilder,
        { provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy }
      ]
    }).compileComponents();

    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
    fb = TestBed.inject(FormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SalaryTsmitComponent);
    component = fixture.componentInstance;
    component.salaryRequirementOptions = enumOptions(YesNo);
    component.visaJobCheck = { id: 123, salaryTsmit: YesNo.Yes };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with visaJobSalaryTsmit control', () => {
    expect(component.form.contains('visaJobSalaryTsmit')).toBeTrue();
  });

  it('should initialize form control with value from visaJobCheck', () => {
    expect(component.form.value.visaJobSalaryTsmit).toEqual(YesNo.Yes );
  });

  it('should render autosave status component', () => {
    const autosaveStatusComponent = fixture.debugElement.query(By.css('app-autosave-status'));
    expect(autosaveStatusComponent).toBeTruthy();
  });

  it('should display error message when error is set', () => {
    component.error = 'Some error message';
    fixture.detectChanges();
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain('Some error message');
  });

  it('should render ng-select with correct placeholder', () => {
    const ngSelect: DebugElement = fixture.debugElement.query(By.css('ng-select'));
    expect(ngSelect).toBeTruthy();
    expect(ngSelect.attributes['placeholder']).toBe('Select');
  });

  it('should display the correct link in the label', () => {
    const link: HTMLElement = fixture.nativeElement.querySelector('a');
    expect(link.textContent).toContain('TSMIT');
    expect(link.getAttribute('href')).toBe('https://immi.homeaffairs.gov.au/visas/employing-and-sponsoring-someone/sponsoring-workers/nominating-a-position/salary-requirements');
  });
});
