import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RelocatingDependantsComponent} from './relocating-dependants.component';
import {CandidateVisaCheckService} from '../../../../../services/candidate-visa-check.service';
import {By} from '@angular/platform-browser';
import {NgSelectModule} from '@ng-select/ng-select';
import {DebugElement} from '@angular/core';
import {DependantRelations} from "../../../../../model/candidate";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";

fdescribe('RelocatingDependantsComponent', () => {
  let component: RelocatingDependantsComponent;
  let fixture: ComponentFixture<RelocatingDependantsComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;
  let fb: FormBuilder;

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      declarations: [RelocatingDependantsComponent,AutosaveStatusComponent],
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
    fixture = TestBed.createComponent(RelocatingDependantsComponent);
    component = fixture.componentInstance;
    component.dependants = [
      { id: 1, relation: DependantRelations.Partner, name: 'John Doe' },
      { id: 2, relation: DependantRelations.Child, name: 'Jane Doe' }
    ];
    component.visaJobCheck = { id: 123, relocatingDependantIds: [1] };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with visaJobRelocatingDependantIds control', () => {
    expect(component.form.contains('visaJobRelocatingDependantIds')).toBeTrue();
  });

  it('should initialize form control with value from visaJobCheck', () => {
    expect(component.form.value.visaJobRelocatingDependantIds).toEqual([1]);
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
    expect(ngSelect.attributes['placeholder']).toBe('Select or type...');
  });

  it('should display the correct helper text', () => {
    const helperText: HTMLElement = fixture.nativeElement.querySelector('.form-text');
    expect(helperText.textContent).toContain("If a dependant isn't listed in the dropdown, you may need to add the dependant to the Dependants section under the Full Intake tab.");
  });
});
