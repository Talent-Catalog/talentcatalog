import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OpportunityStageNextStepComponent} from './opportunity-stage-next-step.component';

describe('OpportunityStageNextStepComponent', () => {
  let component: OpportunityStageNextStepComponent;
  let fixture: ComponentFixture<OpportunityStageNextStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OpportunityStageNextStepComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OpportunityStageNextStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
