import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewCandidateOppComponent} from './view-candidate-opp.component';

describe('ViewCandidateOppComponent', () => {
  let component: ViewCandidateOppComponent;
  let fixture: ComponentFixture<ViewCandidateOppComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateOppComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateOppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
