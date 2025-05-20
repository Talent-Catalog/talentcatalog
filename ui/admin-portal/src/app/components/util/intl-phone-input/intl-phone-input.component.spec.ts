import {ComponentFixture, TestBed} from '@angular/core/testing';

import {IntlPhoneInputComponent} from './intl-phone-input.component';

describe('IntlPhoneInputComponent', () => {
  let component: IntlPhoneInputComponent;
  let fixture: ComponentFixture<IntlPhoneInputComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [IntlPhoneInputComponent]
    });
    fixture = TestBed.createComponent(IntlPhoneInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
