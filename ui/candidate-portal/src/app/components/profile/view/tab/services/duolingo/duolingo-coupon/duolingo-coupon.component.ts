import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Candidate} from "../../../../../../../model/candidate";
import {DuolingoCouponService} from "../../../../../../../services/duolingo-coupon.service";
import {
  TaskAssignmentService,
  UpdateTaskAssignmentRequest
} from "../../../../../../../services/task-assignment.service";
import {TaskAssignment} from "../../../../../../../model/task-assignment";

@Component({
  selector: 'app-duolingo-coupon',
  templateUrl: './duolingo-coupon.component.html',
  styleUrls: ['./duolingo-coupon.component.scss'],
})
export class DuolingoCouponComponent {
  @Input() selectedCoupon: Object;
  @Input() activeDuolingoTask: TaskAssignment;
  @Input() candidate: Candidate;
  @Output() refresh = new EventEmitter();

  loading: boolean;
  error;
  isCollapsedIntro = false;
  isCollapsedSteps = false;

  constructor(private couponService: DuolingoCouponService,
              private taskAssignmentService: TaskAssignmentService) {
  }


  updateSimpleTask() {
    if (this.activeDuolingoTask?.task.name !== 'claimCouponButton') {
      console.log('Task was already updated or is not claimCouponButton.');
      return;
    }

    const request: UpdateTaskAssignmentRequest = {
      completed: true,
      abandoned: false
    };

    this.taskAssignmentService.updateTaskAssignment(this.activeDuolingoTask.id, request).subscribe(
      (taskAssignment) => {
        this.activeDuolingoTask = taskAssignment;
        this.refresh.emit();
      },
      (error) => {
        this.error = error;
      }
    );
  }


}
