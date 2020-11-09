import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DrivingLicenseComponent} from './driving-license.component';

describe('DriversLicenseComponent', () => {
  let component: DrivingLicenseComponent;
  let fixture: ComponentFixture<DrivingLicenseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DrivingLicenseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DrivingLicenseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
