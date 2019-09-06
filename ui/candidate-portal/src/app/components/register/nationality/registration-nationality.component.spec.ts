import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationNationalityComponent} from './registration-nationality.component';

describe('RegistrationNationalityComponent', () => {
  let component: RegistrationNationalityComponent;
  let fixture: ComponentFixture<RegistrationNationalityComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationNationalityComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationNationalityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
