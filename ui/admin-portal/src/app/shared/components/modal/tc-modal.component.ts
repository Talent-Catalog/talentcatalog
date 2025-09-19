import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

/**
 * @component TcModalComponent
 * @selector tc-modal
 * @description
 * A reusable modal wrapper component that includes the NgbModal header and footer.
 * It can be opened either declaratively in a template using <tc-modal>,
 * or programmatically via the NgbModal service.
 *
 * **Features:**
 * - Displays custom modal content with 'ng-content'
 * - Alternatively accepts a simple `message` input for programmatic use cases
 * - Standardises and styles the modal header and footer
 * - Two styles of modal, with icon and without
 * - Outputs the save and dismiss events so the parent component can hook into the action.
 *
 * @example
 *
 *  **Usage in a template (with ng-content):**
 *  ```html
 *  <tc-modal title="Confirmation" icon="fas fa-bell" (onAction)="close()" actionText="Ok">
 *    <span *ngIf="message">{{message}}</span>
 *    <span *ngIf="!message">Are you sure?</span>
 *  </tc-modal>
 *  ```
 *
 *  **Usage programmatically (with message input):**
 *  ```ts
 *  const modalRef = this.modalService.open(TcModalComponent, { size: 'lg' });
 *  modalRef.componentInstance.title = 'Export Failed';
 *  modalRef.componentInstance.icon = 'fas fa-triangle-exclamation';
 *  modalRef.componentInstance.actionText = 'Retry';
 *  modalRef.componentInstance.message = this.error.message;
 *  modalRef.componentInstance.onAction.subscribe(() => {
 *    this.retruMethod();
 *    modalRef.close();
 *  });
 *  ```
 *
 * **With Icon**
 *   ```html
 * <tc-modal title="Confirmation" icon="fas fa-bell" (onAction)="close()" actionText="Ok">
 *     <span *ngIf="message">
 *       {{message}}
 *     </span>
 *
 *     <span *ngIf="!message">
 *       Are you sure?
 *     </span>
 * </tc-modal>
 *   ```
 *
 * **Without Icon**
 *   ```html
 * <tc-modal
 *    title="Add Language"
 *    [disableAction]="form.invalid || loading || saving"
 *    (onAction)="onSave()">
 *    <form [formGroup]="form">
 *      <label for="email">Email</label>
 *      <tc-input
 *        id="email"
 *        name="email"
 *        type="email"
 *        placeholder="you@example.com"
 *        formControlName="email"
 *        [invalid]="form.get('email')?.invalid && form.get('email')?.touched">
 *      </tc-input>
 *    </form>
 * </tc-modal>
 *   ```
 */
@Component({
  selector: 'tc-modal',
  templateUrl: './tc-modal.component.html',
  styleUrls: ['./tc-modal.component.scss']
})
export class TcModalComponent {
  @Input() title: string;
  @Input() actionText: string = 'Save';
  @Input() disableAction: boolean = false;
  @Input() showCancel: boolean = true;
  @Input() icon: string;
  @Input() isError: boolean = false;
  /** Primarily intended for use when declaring modal programmatically via NgbModal service, when
   * ng-content projection not possible since there's no corresponding template element.
   */
  @Input() message?: string;

  @Output() onAction = new EventEmitter();

  constructor(private activeModal: NgbActiveModal) { }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  action() {
    this.onAction.emit();
  }
}
