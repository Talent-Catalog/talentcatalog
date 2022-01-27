import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateTasksComponent} from './candidate-tasks.component';

describe('CandidateTasksComponent', () => {
  let component: CandidateTasksComponent;
  let fixture: ComponentFixture<CandidateTasksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateTasksComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateTasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
