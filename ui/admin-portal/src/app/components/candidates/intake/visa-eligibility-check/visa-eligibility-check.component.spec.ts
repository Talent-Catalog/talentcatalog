import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaEligibilityCheckComponent} from './visa-eligibility-check.component';

describe('VisaEligibilityCheckComponent', () => {
  let component: VisaEligibilityCheckComponent;
  let fixture: ComponentFixture<VisaEligibilityCheckComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaEligibilityCheckComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaEligibilityCheckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
