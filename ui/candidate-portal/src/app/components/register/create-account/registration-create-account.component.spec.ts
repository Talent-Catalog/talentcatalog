import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationCreateAccountComponent} from './registration-create-account.component';

describe('RegistrationCreateAccountComponent', () => {
  let component: RegistrationCreateAccountComponent;
  let fixture: ComponentFixture<RegistrationCreateAccountComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RegistrationCreateAccountComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationCreateAccountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
