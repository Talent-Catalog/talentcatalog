import {Component, EventEmitter, Input, Output, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {of, throwError} from 'rxjs';
import {DuolingoCouponComponent} from './duolingo-coupon.component';
import {DuolingoCouponService} from '../../../../../../../services/duolingo-coupon.service';
import {TaskAssignmentService} from '../../../../../../../services/task-assignment.service';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({selector: 'app-error', template: ''})
class ErrorStubComponent {
  @Input() error: unknown;
}

@Component({selector: 'tc-loading', template: ''})
class TcLoadingStubComponent {
  @Input() loading = false;
}

@Component({selector: 'tc-accordion', template: '<ng-content></ng-content>'})
class TcAccordionStubComponent {
  @Input() showOpenCloseAll = true;
}

@Component({selector: 'tc-accordion-item', template: '<ng-content></ng-content>'})
class TcAccordionItemStubComponent {
  @Input() header?: string;
}

@Component({
  selector: 'tc-button',
  template: '<button (click)="onClick.emit()"><ng-content></ng-content></button>',
})
class TcButtonStubComponent {
  @Input() type?: string;
  @Input() href?: string;
  @Input() target?: string;
  @Output() onClick = new EventEmitter<void>();
}

describe('DuolingoCouponComponent', () => {
  let component: DuolingoCouponComponent;
  let fixture: ComponentFixture<DuolingoCouponComponent>;
  let taskAssignmentServiceSpy: jasmine.SpyObj<TaskAssignmentService>;

  beforeEach(async () => {
    taskAssignmentServiceSpy = jasmine.createSpyObj('TaskAssignmentService', ['updateTaskAssignment']);
    taskAssignmentServiceSpy.updateTaskAssignment.and.returnValue(of({
      id: 9,
      task: {name: 'claimCouponButton'},
    } as any));

    await TestBed.configureTestingModule({
      declarations: [
        DuolingoCouponComponent,
        TranslatePipeStub,
        ErrorStubComponent,
        TcLoadingStubComponent,
        TcAccordionStubComponent,
        TcAccordionItemStubComponent,
        TcButtonStubComponent,
      ],
      providers: [
        {provide: DuolingoCouponService, useValue: {}},
        {provide: TaskAssignmentService, useValue: taskAssignmentServiceSpy},
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DuolingoCouponComponent);
    component = fixture.componentInstance;
    component.selectedCoupon = 'ABC123';
    component.candidate = {
      user: {firstName: 'Hiba', lastName: 'Machfej'},
    } as any;
    component.activeDuolingoTask = {
      id: 9,
      task: {name: 'claimCouponButton'},
    } as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render tc-loading, tc-accordion sections and the CTA tc-button', () => {
    const loading = fixture.debugElement.query(By.directive(TcLoadingStubComponent));
    const accordion = fixture.debugElement.query(By.directive(TcAccordionStubComponent));
    const items = fixture.debugElement.queryAll(By.directive(TcAccordionItemStubComponent));
    const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));

    expect(loading).toBeTruthy();
    expect(accordion).toBeTruthy();
    expect((accordion.componentInstance as TcAccordionStubComponent).showOpenCloseAll).toBeFalse();
    expect(items.length).toBe(3);
    expect(buttons.length).toBe(1);
    expect((buttons[0].componentInstance as TcButtonStubComponent).href).toContain('ABC123');
    expect((buttons[0].componentInstance as TcButtonStubComponent).target).toBe('_blank');
  });

  it('should complete the task and emit refresh when updateSimpleTask succeeds', () => {
    spyOn(component.refresh, 'emit');

    component.updateSimpleTask();

    expect(taskAssignmentServiceSpy.updateTaskAssignment).toHaveBeenCalledWith(9, {
      completed: true,
      abandoned: false,
    });
    expect(component.refresh.emit).toHaveBeenCalled();
  });

  it('should set error when updateSimpleTask fails', () => {
    taskAssignmentServiceSpy.updateTaskAssignment.and.returnValue(throwError('task-error'));

    component.updateSimpleTask();

    expect(component.error).toBe('task-error');
  });
});
