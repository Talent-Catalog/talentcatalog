/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaCheckCardComponent} from './visa-check-card.component';

describe('VisaCheckCardComponent', () => {
  let component: VisaCheckCardComponent;
  let fixture: ComponentFixture<VisaCheckCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaCheckCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
