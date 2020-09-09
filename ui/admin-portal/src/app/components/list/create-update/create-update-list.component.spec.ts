/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateUpdateListComponent} from './create-update-list.component';

describe('UpdateListComponent', () => {
  let component: CreateUpdateListComponent;
  let fixture: ComponentFixture<CreateUpdateListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateUpdateListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUpdateListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
