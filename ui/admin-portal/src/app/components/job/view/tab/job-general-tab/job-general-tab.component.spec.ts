import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobGeneralTabComponent} from './job-general-tab.component';

describe('JobGeneralTabComponent', () => {
  let component: JobGeneralTabComponent;
  let fixture: ComponentFixture<JobGeneralTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobGeneralTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobGeneralTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
