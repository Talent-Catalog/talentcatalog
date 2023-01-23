import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewJobSummaryComponent} from './view-job-summary.component';

describe('ViewJobSummaryComponent', () => {
  let component: ViewJobSummaryComponent;
  let fixture: ComponentFixture<ViewJobSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewJobSummaryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
