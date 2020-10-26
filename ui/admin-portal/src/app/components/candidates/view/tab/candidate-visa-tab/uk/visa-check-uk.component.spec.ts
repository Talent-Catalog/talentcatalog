import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaCheckUkComponent} from './visa-check-uk.component';

describe('VisaCheckUkComponent', () => {
  let component: VisaCheckUkComponent;
  let fixture: ComponentFixture<VisaCheckUkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaCheckUkComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckUkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
