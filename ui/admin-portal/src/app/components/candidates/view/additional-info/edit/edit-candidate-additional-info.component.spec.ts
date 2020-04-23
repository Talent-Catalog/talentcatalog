import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {EditCandidateAdditionalInfoComponent} from './edit-candidate-additional-info.component';

describe('EditCandidateAdditionalInfoComponent', () => {
  let component: EditCandidateAdditionalInfoComponent;
  let fixture: ComponentFixture<EditCandidateAdditionalInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditCandidateAdditionalInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateAdditionalInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
