import {Component, Input} from '@angular/core';

/**
 * @component DescriptionListComponent
 * @selector tc-description-list
 * @input
 * direction - Layout direction: "row" (side by side, single column) or "column" (stacked, responsive columns)
 * @description
 * A reusable wrapper for semantic HTML `<dl>` elements, providing a consistent layout
 * for pairs of terms (`<dt>`) and details (`<dd>`).
 * Works as the parent container for the `tc-description-item` components.
 *
 * **Inputs**
 *  - `direction: 'row' | 'column'` — Layout direction of the list:
 *    - `"row"`: items are displayed side by side, with one item per row.
 *  ```html
 *  description label: description item
 *  ```
 *    - `"column"`: items are stacked vertically, with multiple responsive columns per row.
 *  ```html
 *  description label:
 *  description item
 *  ```
 * - `compact: boolean (defaults to false)` — if true will reduce column spacing when used with column layout
 *
 * **Features**
 * - Wraps the native `<dl>` for semantic, accessible description lists
 * - Works with: `tc-description-item` (containing `<dt>` & `<dd>`)
 * - Provides consistent styling through `.tc-description-list`
 * - Direction type 'column' is responsive using the grid layout

 *
 * @example
 * ```html
 * <!-- Column layout -->
 * <tc-description-list direction="column">
 *   <tc-description-item label="Email">
 *     test@gmail.com
 *   </tc-description-item>
 *  <tc-description-item label="Address">
 *     123 Sesame Street
 *   </tc-description-item>
 * </tc-description-list>
 *
 * <!-- Row layout (default) with icons -->
 * <tc-description-list direction="row">
 *   <tc-description-item label="Country" icon="fas fa-globe">
 *     Australia
*     </tc-description-item>
 *   <tc-description-item label="Employer" icon="fa-solid fa-building">
 *     Google
*     </tc-description-item>
 * </tc-description-list>
 * ```
 */

@Component({
  selector: 'tc-description-list',
  templateUrl: './description-list.component.html',
  styleUrls: ['./description-list.component.scss']
})
export class DescriptionListComponent {
  /** Direction of single item and all items */
  @Input() direction: 'row' | 'column' = 'row';
  /** Smaller spacing for column layout, useful when list is used in compact spaces */
  @Input() compact = false;
}
