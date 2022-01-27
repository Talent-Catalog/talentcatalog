import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EditTaskAssignmentComponent} from './edit-task-assignment.component';

describe('EditTaskAssignmentComponent', () => {
  let component: EditTaskAssignmentComponent;
  let fixture: ComponentFixture<EditTaskAssignmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditTaskAssignmentComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditTaskAssignmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
