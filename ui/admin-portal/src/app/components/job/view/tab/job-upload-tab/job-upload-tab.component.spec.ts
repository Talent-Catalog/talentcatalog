import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobUploadTabComponent} from './job-upload-tab.component';

describe('JobUploadTabComponent', () => {
  let component: JobUploadTabComponent;
  let fixture: ComponentFixture<JobUploadTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobUploadTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobUploadTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
