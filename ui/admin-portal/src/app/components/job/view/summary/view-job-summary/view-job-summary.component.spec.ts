import {ComponentFixture,TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {of,throwError} from 'rxjs';
import {ViewJobSummaryComponent} from './view-job-summary.component';
import {JobService } from '../../../../../services/job.service';
import {Job} from '../../../../../model/job';
import {MockJob} from "../../../../../MockData/MockJob";

describe('ViewJobSummaryComponent', () => {
  let component: ViewJobSummaryComponent;
  let fixture: ComponentFixture<ViewJobSummaryComponent>;
  let jobServiceSpy: jasmine.SpyObj<JobService>;

  beforeEach(async () => {
    const jobServiceSpyObj = jasmine.createSpyObj('JobService', ['updateSummary']);

    await TestBed.configureTestingModule({
      declarations: [ViewJobSummaryComponent],
      imports: [ReactiveFormsModule, HttpClientModule],
      providers: [{ provide: JobService, useValue: jobServiceSpyObj }]
    }).compileComponents();

    jobServiceSpy = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSummaryComponent);
    component = fixture.componentInstance;
    component.job = { id: 1, jobSummary: 'Initial summary' } as Job;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should save changes to job summary correctly', () => {
    const newSummary = 'Updated summary';
    component.form.get('jobSummary').setValue(newSummary);
    jobServiceSpy.updateSummary.and.returnValue(of(MockJob));


    component.saveChanges();

    expect(jobServiceSpy.updateSummary).toHaveBeenCalledWith(component.job.id, newSummary);
    expect(component.error).toBeNull();
    expect(component.saving).toBeFalse();
    expect(component.job.jobSummary).toEqual(newSummary);
    expect(component.jobSummaryControl.pristine).toBeTrue();
  });

  it('should handle error when saving changes to job summary', () => {
    const errorMessage = 'Error saving changes';
    component.form.get('jobSummary').setValue('Updated summary');
    jobServiceSpy.updateSummary.and.returnValue(throwError(errorMessage));

    component.saveChanges();

    expect(jobServiceSpy.updateSummary).toHaveBeenCalled();
    expect(component.error).toEqual(errorMessage);
    expect(component.saving).toBeFalse();
  });

  it('should cancel changes to job summary correctly', () => {
    const originalSummary = component.job.jobSummary;
    component.form.get('jobSummary').setValue('Updated summary');
    component.form.get('jobSummary').markAsDirty();

    component.cancelChanges();

    expect(component.jobSummaryControl.value).toEqual(originalSummary);
    expect(component.jobSummaryControl.pristine).toBeTrue();
  });
});
