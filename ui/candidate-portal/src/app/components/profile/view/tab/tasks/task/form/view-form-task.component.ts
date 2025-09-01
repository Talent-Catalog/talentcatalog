import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {TaskAssignment} from "../../../../../../../model/task-assignment";
import {MyFirstFormComponent} from "../../../../../../form/my-first-form/my-first-form.component";

@Component({
  selector: 'app-view-form-task',
  templateUrl: './view-form-task.component.html',
  styleUrls: ['./view-form-task.component.scss']
})
export class ViewFormTaskComponent implements OnChanges {
  @Input() selectedTask: TaskAssignment;

  error: string;

  /*
      You need to add an entry to this map for each form that can be displayed in a FormTask.
      The mapping is from the name of the form to an Angular component.
   */
  componentMap: Record<string, any> = {
    'MyFirstForm': MyFirstFormComponent
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.selectedTask) {
      //Check that we have an Angular component mapped to the name of the form associated with
      //the selected task.
      let task = this.selectedTask.task;
      let formName = task.candidateForm.name;
      let component =  this.componentMap[formName];
      if (component) {
        this.error = null;
      } else {
        this.error = 'Angular ViewFormTaskComponent: No Component found matching Candidate Form '
          + formName + ', associated with Form Task ' + task.name
          + '. Add a mapping to the componentMap in ViewFormTaskComponent.ts.';
      }
    }
  }

  /**
   * Returns the Angular component containing the form to be displayed.
   * <p/>
   * May return undefined/null in which case no component will be displayed.
   */
  get selectedForm() {
    let formName = this.selectedTask.task.candidateForm.name;
    return this.componentMap[formName];
  }
}
