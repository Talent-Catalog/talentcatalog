import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EventEmitter } from '@angular/core';
import { JobUploadTabComponent } from './job-upload-tab.component';
import { Job } from '../../../../../model/job';
import { JobPrepItem } from '../../../../../model/job-prep-item';
import {MockJob} from "../../../../../MockData/MockJob";
import {ViewJobUploadsComponent} from "../../uploads/view-job-uploads/view-job-uploads.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";

fdescribe('JobUploadTabComponent', () => {
  let component: JobUploadTabComponent;
  let fixture: ComponentFixture<JobUploadTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports:[HttpClientTestingModule,LocalStorageModule.forRoot({})],
      declarations: [ JobUploadTabComponent ,ViewJobUploadsComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobUploadTabComponent);
    component = fixture.componentInstance;
    component.job = MockJob;
    fixture.detectChanges();
  });

  it('should emit jobUpdated event when onJobUpdated is called', () => {
    const job: Job = {...MockJob};
    spyOn(component.jobUpdated, 'emit');

    component.onJobUpdated(job);

    expect(component.jobUpdated.emit).toHaveBeenCalledWith(job);
  });

});
