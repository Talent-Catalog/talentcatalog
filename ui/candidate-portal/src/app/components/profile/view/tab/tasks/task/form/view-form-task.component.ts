import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  Type,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import {TaskAssignment} from "../../../../../../../model/task-assignment";
import {MyFirstFormComponent} from "../../../../../../form/my-first-form/my-first-form.component";

/**
 * Interface of any component which has an @Output() submitted EventEmitter
 */
export interface HasSubmitted<T = unknown> {
  submitted: import("@angular/core").EventEmitter<T>;
}

@Component({
  selector: 'app-view-form-task',
  template: `
    <app-error [error]="error"></app-error>
    <ng-template #vc></ng-template>`,
  styleUrls: ['./view-form-task.component.scss']
})
export class ViewFormTaskComponent implements OnChanges {
  @Input() selectedTask: TaskAssignment;

  //Output event supplying the submitted data
  @Output() taskCompleted = new EventEmitter<TaskAssignment>();

  //This refers to the #vc component in the template defined above in @Component
  @ViewChild('vc',{read: ViewContainerRef, static: true}) vc?: ViewContainerRef;

  //Name of the currently loaded form. Used to avoid reloading a form that is already loaded.
  currentlyLoadedFormName: string = null;

  //Used to record errors in mapping the form name to an Angular component.
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
      if (component && formName != this.currentlyLoadedFormName) {
        //If we have a valid component which is not already loaded into the html, add it to the html
        this.load(component);
        this.currentlyLoadedFormName = formName;
      } else {
        this.error = 'Angular ViewFormTaskComponent: No Component found matching Candidate Form '
          + formName + ', associated with Form Task ' + task.name
          + '. Add a mapping to the componentMap in ViewFormTaskComponent.ts.';
      }
    }
  }

  /**
   * Loads a component which extends HasSubmitted into the template.
   * @param cmp Component to be loaded into the html template.
   */
  load<C extends HasSubmitted>(cmp: Type<C>){
    this.vc.clear();
    const ref = this.vc.createComponent(cmp);
    ref.instance.submitted.subscribe(v => this.onSubmitted(v));
  }

  onSubmitted() {
    this.taskCompleted.emit(this.selectedTask);
  }
}
