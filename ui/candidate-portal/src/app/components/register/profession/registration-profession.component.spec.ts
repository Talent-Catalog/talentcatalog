import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationProfessionComponent} from './registration-profession.component';

describe('RegistrationProfessionComponent', () => {
  let component: RegistrationProfessionComponent;
  let fixture: ComponentFixture<RegistrationProfessionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationProfessionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationProfessionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
