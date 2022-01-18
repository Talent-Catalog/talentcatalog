import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Candidate, TaskAssignment} from "../../../../../model/candidate";
import {CandidateService} from "../../../../../services/candidate.service";

@Component({
  selector: 'app-candidate-task-tab',
  templateUrl: './candidate-task-tab.component.html',
  styleUrls: ['./candidate-task-tab.component.scss']
})
export class CandidateTaskTabComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  loading;
  error;
  saving;
  ongoingTasks: TaskAssignment[];
  completedTasks: TaskAssignment[];
  today: Date;

  constructor(private candidateService: CandidateService) { }

  ngOnInit(): void {
    this.today = new Date();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.loading = true;
      this.candidateService.get(this.candidate.id).subscribe(
        candidate => {
          this.candidate = candidate;
          this.ongoingTasks = this.candidate.taskAssignments.filter(t => t.completedDate == null);
          this.completedTasks = this.candidate.taskAssignments.filter(t => t.completedDate != null);
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        });
    }
  }

  isOverdue(ta: TaskAssignment) {
    return (new Date(ta.dueDate) < this.today) && !ta.task.optional;
  }


}
