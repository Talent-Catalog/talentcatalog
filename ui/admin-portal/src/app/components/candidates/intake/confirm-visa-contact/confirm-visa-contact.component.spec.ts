import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ConfirmVisaContactComponent} from './confirm-visa-contact.component';

describe('ConfirmVisaContactComponent', () => {
  let component: ConfirmVisaContactComponent;
  let fixture: ComponentFixture<ConfirmVisaContactComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfirmVisaContactComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmVisaContactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
