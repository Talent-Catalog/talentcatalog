import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerifyEmailToastComponent } from './verify-email-toast.component';

describe('VerifyEmailToastComponent', () => {
  let component: VerifyEmailToastComponent;
  let fixture: ComponentFixture<VerifyEmailToastComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [VerifyEmailToastComponent]
    });
    fixture = TestBed.createComponent(VerifyEmailToastComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
