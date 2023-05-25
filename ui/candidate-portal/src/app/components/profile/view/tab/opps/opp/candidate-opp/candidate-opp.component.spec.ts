import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateOppComponent} from './candidate-opp.component';

describe('CandidateOppComponent', () => {
  let component: CandidateOppComponent;
  let fixture: ComponentFixture<CandidateOppComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateOppComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateOppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
