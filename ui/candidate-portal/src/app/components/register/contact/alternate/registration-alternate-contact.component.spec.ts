import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationAlternateContactComponent} from './registration-alternate-contact.component';

describe('RegistrationAlternateContactComponent', () => {
  let component: RegistrationAlternateContactComponent;
  let fixture: ComponentFixture<RegistrationAlternateContactComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationAlternateContactComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationAlternateContactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
