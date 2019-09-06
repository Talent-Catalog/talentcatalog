import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationLandingComponent} from './registration-landing.component';

describe('RegistrationLandingComponent', () => {
  let component: RegistrationLandingComponent;
  let fixture: ComponentFixture<RegistrationLandingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationLandingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationLandingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
