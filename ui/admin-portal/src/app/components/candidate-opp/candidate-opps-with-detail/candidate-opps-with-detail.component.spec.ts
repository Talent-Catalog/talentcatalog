import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateOppsWithDetailComponent} from './candidate-opps-with-detail.component';

describe('CandidateOppsWithDetailComponent', () => {
  let component: CandidateOppsWithDetailComponent;
  let fixture: ComponentFixture<CandidateOppsWithDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateOppsWithDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateOppsWithDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
