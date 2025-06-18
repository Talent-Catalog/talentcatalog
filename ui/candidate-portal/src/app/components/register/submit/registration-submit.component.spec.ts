import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistrationSubmitComponent } from './registration-submit.component';

describe('SubmitComponent', () => {
  let component: RegistrationSubmitComponent;
  let fixture: ComponentFixture<RegistrationSubmitComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RegistrationSubmitComponent]
    });
    fixture = TestBed.createComponent(RegistrationSubmitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
