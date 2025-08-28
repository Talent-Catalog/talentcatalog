import { Component } from '@angular/core';

/**
 * @component DescriptionDetailsComponent
 * @selector tc-description-details
 * @description
 * Represents the **details** (`<dd>`) element inside a semantic description list.
 * Should be used as a child of `tc-description-list`, paired with a `tc-description-term`.
 *
 * **Features**
 * - Wraps the native `<dd>` tag
 * - Projects any text or markup using `<ng-content>`
 * - Provides consistent styling with the `.tc-description-details` class
 * - Enhances readability and accessibility when used with `tc-description-term`
 *
 * @example
 * ```html
 * <tc-description-list direction="column">
 *   <tc-description-term>Submission Due</tc-description-term>
 *   <tc-description-details>2023-05-12</tc-description-details>
 *
 *   <tc-description-term>Created</tc-description-term>
 *   <tc-description-details>2023-03-14, 7:41:44 AM Rabia</tc-description-details>
 *
 *   <tc-description-term>Updated</tc-description-term>
 *   <tc-description-details>2025-03-28, 4:49:20 AM Sam Schlicht</tc-description-details>
 * </tc-description-list>
 * ```
 */

@Component({
  selector: 'tc-description-details',
  templateUrl: './description-details.component.html',
  styleUrls: ['./description-details.component.scss']
})
export class DescriptionDetailsComponent {

}
