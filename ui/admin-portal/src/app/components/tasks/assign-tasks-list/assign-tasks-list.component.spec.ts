import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AssignTasksListComponent} from './assign-tasks-list.component';

describe('AssignTasksListComponent', () => {
  let component: AssignTasksListComponent;
  let fixture: ComponentFixture<AssignTasksListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssignTasksListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignTasksListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
