import { Component, Input } from '@angular/core';

/**
 * @component DescriptionListComponent
 * @selector tc-description-list
 * @input
 * direction - Layout direction: "row" (side by side) or "column" (stacked)
 * @description
 * A reusable wrapper for semantic HTML `<dl>` elements, providing a consistent layout
 * for displaying pairs of terms (`tc-description-term`) and details (`tc-description-details`).
 * Works as the parent container in the description list system.
 *
 * **Inputs**
 *  - `direction: 'row' | 'column'` — Layout direction of the list:
 *  - `"row"`: items are displayed side by side
 *  - `"column"`: items are stacked vertically
 *
 * **Features**
 * - Wraps the native `<dl>` for semantic, accessible description lists
 * - Works together with:
 *   - `tc-description-term` (for `<dt>`)
 *   - `tc-description-details` (for `<dd>`)
 * - Provides consistent styling through `.tc-description-list`

 *
 * @example
 * ```html
 * <!-- Column layout (default) -->
 * <tc-description-list direction="column">
 *   <tc-description-term>Email</tc-description-term>
 *   <tc-description-details>user@example.com</tc-description-details>
 *
 *   <tc-description-term>Username</tc-description-term>
 *   <tc-description-details>my-username</tc-description-details>
 * </tc-description-list>
 *
 * <!-- Row layout with custom class -->
 * <tc-description-list direction="row">
 *   <tc-description-term>Country</tc-description-term>
 *   <tc-description-details>Australia</tc-description-details>
 *
 *   <tc-description-term>Employer</tc-description-term>
 *   <tc-description-details><u>Blue Mountains Highway Motel</u></tc-description-details>
 * </tc-description-list>
 * ```
 */

@Component({
  selector: 'tc-description-list',
  templateUrl: './description-list.component.html',
  styleUrls: ['./description-list.component.scss']
})
export class DescriptionListComponent {
  @Input() direction: 'row' | 'column' = 'row';
}
