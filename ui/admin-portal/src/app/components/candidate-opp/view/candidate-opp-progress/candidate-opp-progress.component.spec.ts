import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateOppProgressComponent} from './candidate-opp-progress.component';

describe('CandidateOppProgressComponent', () => {
  let component: CandidateOppProgressComponent;
  let fixture: ComponentFixture<CandidateOppProgressComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateOppProgressComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateOppProgressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
