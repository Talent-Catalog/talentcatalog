import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewCandidateTasksComponent} from './view-candidate-tasks.component';

describe('ViewCandidateTasksComponent', () => {
  let component: ViewCandidateTasksComponent;
  let fixture: ComponentFixture<ViewCandidateTasksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateTasksComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateTasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
