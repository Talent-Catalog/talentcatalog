import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationAdditionalInfoComponent} from './registration-additional-info.component';

describe('RegistrationAdditionalInfoComponent', () => {
  let component: RegistrationAdditionalInfoComponent;
  let fixture: ComponentFixture<RegistrationAdditionalInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationAdditionalInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationAdditionalInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
