import {ComponentFixture, TestBed} from '@angular/core/testing';

import {MonitoringEvaluationConsentComponent} from './monitoring-evaluation-consent.component';

describe('MonitoringEvaluationConsentComponent', () => {
  let component: MonitoringEvaluationConsentComponent;
  let fixture: ComponentFixture<MonitoringEvaluationConsentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MonitoringEvaluationConsentComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MonitoringEvaluationConsentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
