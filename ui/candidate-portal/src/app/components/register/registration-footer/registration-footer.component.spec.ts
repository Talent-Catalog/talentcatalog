import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationFooterComponent} from './registration-footer.component';

describe('RegistrationFooterComponent', () => {
  let component: RegistrationFooterComponent;
  let fixture: ComponentFixture<RegistrationFooterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationFooterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationFooterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
