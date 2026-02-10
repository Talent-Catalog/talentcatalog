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
 *   **Inputs**
 *  - `heading: string` — title text shown in the modal header
 *  - `actionText: string = 'Save'` — label for the action button
 *  - `disableAction: boolean` — disables the action button
 *  - `showCancel: boolean` — toggles the cancel button visibility
 *  - `icon: string` — optional FontAwesome icon class
 *  - `isError: boolean = false` — switches the modal to an error style (red header + red icon)
 *  - `cancelText: string = 'Cancel'` — label for the cancel button
 *  - `showClose: boolean = false` — toggles the top-right close “X”
 *  - `message?: string` — plain message text for programmatic modals
 *  - `showAction: boolean = true` — toggles the primary action button
 *
 *  **Outputs**
 *  - `onAction` — emits when the action button is clicked
 *
 * @example
 *
 *  **Usage in a template (with ng-content):**
 *  ```html
 *  <tc-modal heading="Confirmation" icon="fas fa-bell" (onAction)="close()" actionText="Ok">
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
 *    this.returnMethod();
 *    modalRef.close();
 *  });
 *  ```
 *
 * **With Icon**
 *   ```html
 * <tc-modal heading="Confirmation" icon="fas fa-bell" (onAction)="close()" actionText="Ok">
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
 *    heading="Add Language"
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
  /** Appears as the title of the modal (note: reason the input isn't called title is that the
   * title attribute applies a tooltip across the whole component */
  @Input() heading: string;
  @Input() actionText: string = 'Save';
  /** Disable the action button e.g. on saving/loading status */
  @Input() disableAction: boolean = false;
  @Input() showCancel: boolean = true;
  @Input() icon: string;
  @Input() isError: boolean = false;
  @Input() cancelText: string = 'Cancel';
  /**
   * Places an 'x' close button in the top right-hand corner of the modal — typically used in larger
   * modals or when showCancel is set to false.
   */
  @Input() showClose: boolean = false;
  /** Primarily intended for use when declaring modal programmatically via NgbModal service, when
   * ng-content projection not possible since there's no corresponding template element.
   */
  @Input() message?: string;
  @Input() showAction: boolean = true;

  @Output() onAction = new EventEmitter();

  constructor(private activeModal: NgbActiveModal) { }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  action() {
    this.onAction.emit();
  }

  protected readonly close = close;
}
