import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationLanguageComponent} from './registration-language.component';

describe('RegistrationLanguageComponent', () => {
  let component: RegistrationLanguageComponent;
  let fixture: ComponentFixture<RegistrationLanguageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationLanguageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationLanguageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
