import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateTaskComponent} from './candidate-task.component';

describe('CandidateTaskComponent', () => {
  let component: CandidateTaskComponent;
  let fixture: ComponentFixture<CandidateTaskComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateTaskComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
