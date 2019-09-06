import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationLocationComponent} from './registration-location.component';

describe('RegistrationLocationComponent', () => {
  let component: RegistrationLocationComponent;
  let fixture: ComponentFixture<RegistrationLocationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationLocationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationLocationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
