import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

/**
 * @component TcModalComponent
 * @selector tc-modal
 * @description
 * A reusable modal wrapper component that includes the NgbModal header and footer. It wraps the
 * modal content so it can be fully customizable.
 *
 * **Features:**
 * - Displays the custom modal content
 * - Standardises and styles the modal header and footer
 * - Two styles of modal, with icon and without
 * - Outputs the save and dismiss events so the parent component can hook into the action.
 *
 * @example
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
  @Input() header: string;
  @Input() actionText: string = 'Save';
  /** Disable the action button e.g. on saving/loading status */
  @Input() disableAction: boolean = false;
  @Input() showCancel: boolean = true;
  @Input() icon: string;

  @Output() onAction = new EventEmitter();

  constructor(private activeModal: NgbActiveModal) { }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  action() {
    this.onAction.emit();
  }
}
