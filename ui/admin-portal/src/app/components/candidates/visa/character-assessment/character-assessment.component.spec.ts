import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CharacterAssessmentComponent} from './character-assessment.component';

describe('CharacterAssessmentComponent', () => {
  let component: CharacterAssessmentComponent;
  let fixture: ComponentFixture<CharacterAssessmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CharacterAssessmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CharacterAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
