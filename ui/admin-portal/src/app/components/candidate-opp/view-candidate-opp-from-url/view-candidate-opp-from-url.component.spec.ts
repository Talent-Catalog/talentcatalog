import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewCandidateOppFromUrlComponent} from './view-candidate-opp-from-url.component';

describe('ViewCandidateOppFromUrlComponent', () => {
  let component: ViewCandidateOppFromUrlComponent;
  let fixture: ComponentFixture<ViewCandidateOppFromUrlComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateOppFromUrlComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateOppFromUrlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
