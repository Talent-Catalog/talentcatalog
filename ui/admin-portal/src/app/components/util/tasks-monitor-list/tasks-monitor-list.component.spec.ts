import {TasksMonitorListComponent} from "./tasks-monitor-list.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {TaskAssignmentService} from "../../../services/task-assignment.service";
import {TaskAssignment} from "../../../model/task-assignment";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {of, throwError} from "rxjs";
import {NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";

describe('TasksMonitorListComponent', () => {
  let component: TasksMonitorListComponent;
  let fixture: ComponentFixture<TasksMonitorListComponent>;
  let taskAssignmentServiceSpy: jasmine.SpyObj<TaskAssignmentService>;
  const mockTaskAssignments: TaskAssignment[] = new MockCandidate().taskAssignments;

  beforeEach(async () => {
    const taskAssignmentServiceMock = jasmine.createSpyObj('TaskAssignmentService', ['search']);

    await TestBed.configureTestingModule({
      imports: [NgbTooltipModule],
      declarations: [ TasksMonitorListComponent ],
      providers: [
        { provide: TaskAssignmentService, useValue: taskAssignmentServiceMock }
      ]
    })
    .compileComponents();

    taskAssignmentServiceSpy = TestBed.inject(TaskAssignmentService) as jasmine.SpyObj<TaskAssignmentService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TasksMonitorListComponent);
    component = fixture.componentInstance;
    component.task = { id: 1, name: 'Sample Task' } as any; // Mock task
    component.list = { id: 1, name: 'Sample List' } as any; // Mock list
    taskAssignmentServiceSpy.search.and.returnValue(of(mockTaskAssignments));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize task assignments and counts', () => {
    component.ngOnInit();
    expect(component.taskAssignments).toEqual(mockTaskAssignments);
    expect(component.completed.length).toBe(0);
    expect(component.abandoned.length).toBe(0);
    expect(component.outstandingNotOverdue.length).toBe(2); // Two outstanding not overdue task
    expect(component.outstandingOverdue.length).toBe(1); // One outstanding overdue task
  });

  it('should handle error when fetching task assignments', () => {
    const errorMessage = 'Error fetching task assignments';
    taskAssignmentServiceSpy.search.and.returnValue(throwError(errorMessage));

    component.ngOnInit();

    expect(component.error).toBe(errorMessage);
  });

  it('should handle empty task assignments', () => {
    const mockTaskAssignments: TaskAssignment[] = [];

    taskAssignmentServiceSpy.search.and.returnValue(of(mockTaskAssignments));

    component.ngOnInit();

    expect(component.taskAssignments).toEqual(mockTaskAssignments);
    expect(component.completed.length).toBe(0);
    expect(component.abandoned.length).toBe(0);
    expect(component.outstandingNotOverdue.length).toBe(0);
    expect(component.outstandingOverdue.length).toBe(0);
  });

});

