import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationCertificationsComponent} from './registration-certifications.component';

describe('RegistrationCertificationsComponent', () => {
  let component: RegistrationCertificationsComponent;
  let fixture: ComponentFixture<RegistrationCertificationsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationCertificationsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationCertificationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
