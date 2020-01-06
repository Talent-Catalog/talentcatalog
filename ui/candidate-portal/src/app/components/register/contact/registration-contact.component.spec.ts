import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationContactComponent} from './registration-contact.component';

describe('RegistrationContactComponent', () => {
  let component: RegistrationContactComponent;
  let fixture: ComponentFixture<RegistrationContactComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationContactComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationContactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
