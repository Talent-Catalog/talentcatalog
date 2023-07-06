import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateSideProfileComponent} from './candidate-side-profile.component';

describe('CandidateSideProfileComponent', () => {
  let component: CandidateSideProfileComponent;
  let fixture: ComponentFixture<CandidateSideProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateSideProfileComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateSideProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
