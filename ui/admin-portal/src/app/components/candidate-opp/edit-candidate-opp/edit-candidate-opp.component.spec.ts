import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EditCandidateOppComponent} from './edit-candidate-opp.component';

describe('SalesforceStageComponent', () => {
  let component: EditCandidateOppComponent;
  let fixture: ComponentFixture<EditCandidateOppComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditCandidateOppComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateOppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
