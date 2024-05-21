import {ComponentFixture, TestBed} from '@angular/core/testing';
import {JobGeneralTabComponent} from './job-general-tab.component';
import {MockJob} from "../../../../../MockData/MockJob";
import {ViewJobInfoComponent} from "../../info/view-job-info/view-job-info.component";
import {RouterLinkStubDirective} from "../../../../login/login.component.spec";
import {Job} from "../../../../../model/job";

fdescribe('JobGeneralTabComponent', () => {
  let component: JobGeneralTabComponent;
  let fixture: ComponentFixture<JobGeneralTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [JobGeneralTabComponent,ViewJobInfoComponent,RouterLinkStubDirective],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobGeneralTabComponent);
    component = fixture.componentInstance;
    fixture.componentInstance.job = MockJob;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit jobUpdated event when job is updated', () => {
    // Create a shallow copy of MockJob
    const updatedJob: Job = { ...MockJob };

    // Modify the properties of updatedJob as needed
    updatedJob.name = 'Updated Name';
    updatedJob.jobSummary = 'Updated Job Summary';

    spyOn(component.jobUpdated, 'emit');

    component.onJobUpdated(updatedJob);
    // Check if the jobUpdated event is emitted with the correct argument
    expect(component.jobUpdated.emit).toHaveBeenCalledWith(updatedJob);
  });
});
