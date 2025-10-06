import {Component, Input} from '@angular/core';

/**
 * @component DescriptionItemComponent
 * @selector tc-description-item
 * @input
 * label - string for the term (`<dt>`), required.
 * icon - font awesome icon class to display in the term, optional.
 * @description
 * Reuseable component that groups the pair of term (`<dt>`) and detail (`<dd>`). The input/s form
 * the term, and the component wraps the detail.
 *
 * **Features**
 * - Wraps the (`<dt>` & `<dd>`) elements in a single component
 * - Can have *ngIf or other conditional statements applied

 *
 * @example
 * ```html

 * <tc-description-list direction="column">
 *   <!-- Conditional description item -->
 *   <tc-description-item *ngIf="canSeeEmail()" label="Email">
 *     <a href="mailto: test@gmail.com">test@gmail.com</a>
 *   </tc-description-item>
*    <!-- Term with icon -->
 *  <tc-description-item label="Address" icon="fa-solid fa-building">
 *    123 Sesame Street
 *   </tc-description-item>
 * </tc-description-list>
 * ```
 */
@Component({
  selector: 'tc-description-item',
  templateUrl: './description-item.component.html',
  styleUrls: ['./description-item.component.scss']
})
export class DescriptionItemComponent {
  @Input() label!: string;
  @Input() icon?: string;
}
