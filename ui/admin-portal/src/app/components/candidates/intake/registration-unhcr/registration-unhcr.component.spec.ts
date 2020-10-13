import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationUnhcrComponent} from './registration-unhcr.component';

describe('RegistrationUnhcrComponent', () => {
  let component: RegistrationUnhcrComponent;
  let fixture: ComponentFixture<RegistrationUnhcrComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationUnhcrComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationUnhcrComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
