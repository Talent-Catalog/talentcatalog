import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SecurityRiskComponent} from './security-risk.component';

describe('SecurityAssessmentComponent', () => {
  let component: SecurityRiskComponent;
  let fixture: ComponentFixture<SecurityRiskComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecurityRiskComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurityRiskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
