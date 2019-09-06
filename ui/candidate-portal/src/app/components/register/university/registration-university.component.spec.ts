import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationUniversityComponent} from './registration-university.component';

describe('RegistrationUniversityComponent', () => {
  let component: RegistrationUniversityComponent;
  let fixture: ComponentFixture<RegistrationUniversityComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationUniversityComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationUniversityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
