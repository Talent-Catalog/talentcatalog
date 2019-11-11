import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewCandidateAdditionalInfoComponent} from './view-candidate-additional-info.component';

describe('ViewCandidateAdditionalInfoComponent', () => {
  let component: ViewCandidateAdditionalInfoComponent;
  let fixture: ComponentFixture<ViewCandidateAdditionalInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewCandidateAdditionalInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateAdditionalInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
