import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationUnrwaComponent} from './registration-unrwa.component';

describe('RegistrationUnrwaComponent', () => {
  let component: RegistrationUnrwaComponent;
  let fixture: ComponentFixture<RegistrationUnrwaComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationUnrwaComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationUnrwaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
