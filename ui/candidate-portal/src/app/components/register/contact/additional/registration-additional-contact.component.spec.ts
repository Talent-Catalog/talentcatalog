import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationAdditionalContactComponent} from './registration-additional-contact.component';

describe('RegistrationAdditionalContactComponent', () => {
  let component: RegistrationAdditionalContactComponent;
  let fixture: ComponentFixture<RegistrationAdditionalContactComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationAdditionalContactComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationAdditionalContactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
