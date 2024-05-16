import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
  waitForAsync
} from '@angular/core/testing';
import { JobBenefitsComponent } from './job-benefits.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { JobService } from '../../../../services/job.service';
import { of } from 'rxjs';
import { AutosaveStatusComponent } from '../../../util/autosave-status/autosave-status.component';
import { NgxWigModule } from 'ngx-wig';

fdescribe('JobBenefitsComponent', () => {
  let component: JobBenefitsComponent;
  let fixture: ComponentFixture<JobBenefitsComponent>;
  let jobServiceSpy: jasmine.SpyObj<JobService>;

  beforeEach(waitForAsync(() => {
    const jobServiceSpyObj = jasmine.createSpyObj('JobService', ['updateIntakeData']);

    TestBed.configureTestingModule({
      declarations: [JobBenefitsComponent, AutosaveStatusComponent],
      imports: [ReactiveFormsModule, NgxWigModule],
      providers: [
        FormBuilder,
        { provide: JobService, useValue: jobServiceSpyObj }
      ]
    })
    .compileComponents();
    jobServiceSpy = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
    jobServiceSpy.updateIntakeData.and.returnValue(of(null)); // Mocking save response
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(JobBenefitsComponent);
    component = fixture.componentInstance;
    component.jobIntakeData = { benefits: 'Test benefits' };
    component.editable = true;
    component.entity = { id: 1 }; // Initialize the entity property
    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should initialize form with jobIntakeData if provided', () => {
    const testData = { benefits: 'Test benefits' };
    component.jobIntakeData = testData;
    component.editable = true;
    component.ngOnInit();
    expect(component.form.value.benefits).toEqual(testData.benefits);
  });

  it('should disable form control when editable is false', () => {
    component.editable = false;
    component.ngOnInit();
    expect(component.form.controls.benefits.disabled).toBeTrue();
  });
  it('should update jobIntakeData and call updateIntakeData on successful save', fakeAsync(() => {
    // Arrange
    const testData = 'Test benefits';
    jobServiceSpy.updateIntakeData.and.returnValue(of(null)); // Mocking save response
    let saveCompleted = false;
    // Act
    component.onSuccessfulSave();
    component.doSave({ benefits: testData }).subscribe(() => {
      saveCompleted = true;
    });
    tick(); // Wait for asynchronous operations to complete
    fixture.detectChanges();

    // Assert
    expect(saveCompleted).toBeTrue();
    expect(jobServiceSpy.updateIntakeData).toHaveBeenCalledTimes(1);
    expect(jobServiceSpy.updateIntakeData).toHaveBeenCalledWith(1, { benefits: testData });
    expect(component.jobIntakeData.benefits).toEqual(testData);
  }));
});
