/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaAssessmentComponent} from './visa-check-card.component';

describe('VisaCheckCardComponent', () => {
  let component: VisaAssessmentComponent;
  let fixture: ComponentFixture<VisaAssessmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaAssessmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
