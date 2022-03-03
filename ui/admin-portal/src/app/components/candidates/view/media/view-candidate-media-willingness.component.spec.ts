import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewCandidateMediaWillingnessComponent} from './view-candidate-media-willingness.component';

describe('ViewCandidateMediaWillingnessComponent', () => {
  let component: ViewCandidateMediaWillingnessComponent;
  let fixture: ComponentFixture<ViewCandidateMediaWillingnessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateMediaWillingnessComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateMediaWillingnessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
