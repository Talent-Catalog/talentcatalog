import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EditJobSummaryComponent} from './edit-job-summary.component';

describe('EditJobSummaryComponent', () => {
  let component: EditJobSummaryComponent;
  let fixture: ComponentFixture<EditJobSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditJobSummaryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditJobSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
