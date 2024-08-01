import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateVisaJobComponent} from './candidate-visa-job.component';
// todo these tests - maybe if these are completed it might fix the bug?
describe('CandidateVisaJobComponent', () => {
  let component: CandidateVisaJobComponent;
  let fixture: ComponentFixture<CandidateVisaJobComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateVisaJobComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateVisaJobComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
