import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationMastersComponent} from './registration-masters.component';

describe('RegistrationMastersComponent', () => {
  let component: RegistrationMastersComponent;
  let fixture: ComponentFixture<RegistrationMastersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationMastersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationMastersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
