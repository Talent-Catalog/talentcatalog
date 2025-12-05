import {
  Component,
  ComponentRef,
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
import {CandidateFormService} from "../../../../../../../services/candidate-form.service";
import {ICandidateFormComponent} from "../../../../../../../model/candidate-form";
import {Candidate} from "../../../../../../../model/candidate";

/**
 * This component loads and displays the form associated with the input task assignment.
 * <p/>
 * The task assignment is marked as complete when the form is submitted.
 *
 * @author John Cameron
 */
@Component({
  selector: 'app-view-form-task',
  template: `
    <app-error [error]="error"></app-error>
    <ng-template #vc></ng-template>`,
  styleUrls: ['./view-form-task.component.scss']
})
export class ViewFormTaskComponent implements OnChanges {
  @Input() taskAssignment: TaskAssignment;
  @Input() candidate: Candidate;
  @Input() readOnly: boolean = false;

  //Output event supplying the submitted data
  @Output() taskCompleted = new EventEmitter<TaskAssignment>();

  //This refers to the #vc component in the template defined above in @Component
  @ViewChild('vc',{read: ViewContainerRef, static: true}) vc?: ViewContainerRef;

  //Name of the currently loaded form. Used to avoid reloading a form that is already loaded.
  currentlyLoadedFormName: string = null;
  currentlyLoadedFormRef: ComponentRef<any> = null;

  //Used to record errors in mapping the form name to an Angular component.
  error: string;

  constructor(private candidateFormService: CandidateFormService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.readOnly) {
      this.setFormReadOnly(this.readOnly);
    }
    if (changes.taskAssignment) {
      //Check that we have an Angular component mapped to the name of the form associated with
      //the selected task.
      let task = this.taskAssignment.task;
      let formName = task.candidateForm?.name;
      if (!formName) {
        this.error = 'Angular ViewFormTaskComponent: Task ' + task.name + ' is not a FormTask.';
        return;
      }
      let component =  this.candidateFormService.getFormComponentByName(formName);
      if (component) {
        if (formName != this.currentlyLoadedFormName) {
          //We have a valid component which is not already loaded into the HTML. Add it.
          this.currentlyLoadedFormRef = this.load(component);
          this.currentlyLoadedFormName = formName;

          //If the task is completed or abandoned, then the component should be read-only.
          let readOnly = this.taskAssignment.completedDate != null
            || this.taskAssignment.abandonedDate != null;
          this.setFormReadOnly(readOnly);
        }
      } else {
        this.error = 'Angular ViewFormTaskComponent: No Component found matching Candidate Form '
          + formName + ', associated with Form Task ' + task.name
          + '. Add a mapping to the componentMap in CandidateFormService.ts.';
      }
    }
  }

  /**
   * Loads a component which looks like ICandidateFormComponent into the template.
   * @param cmp Component to be loaded into the HTML template.
   * @return A reference to the loaded component
   */
  load<C extends ICandidateFormComponent>(cmp: Type<C>){
    this.vc.clear();
    const ref = this.vc.createComponent(cmp);
    ref.setInput('candidate', this.candidate);
    this.setFormReadOnly(this.readOnly);
    //Subscribe to submitted events
    ref.instance.submitted.subscribe(() => this.onSubmitted());

    return ref;
  }

  onSubmitted() {
    this.taskCompleted.emit(this.taskAssignment);
    this.setFormReadOnly(true);

  }

  private setFormReadOnly(readOnly: boolean) {
    if (this.currentlyLoadedFormRef) {
      this.currentlyLoadedFormRef.setInput('readOnly', readOnly);
    }
  }
}
