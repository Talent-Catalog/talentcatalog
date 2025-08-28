import { Component } from '@angular/core';

/**
 * @component DescriptionTermComponent
 * @selector tc-description-term
 * @description
 * Represents the **term** (`<dt>`) element inside a semantic description list.
 * Should be used as a child of `tc-description-list`, and always paired with
 * `tc-description-details`.
 *
 * **Features**
 * - Wraps the native `<dt>` tag
 * - Projects any text or markup using `<ng-content>`
 * - Provides consistent styling with the `.tc-description-term` class
 * - Enhances readability and accessibility when used inside a description list
 *
 * @example
 * ```html
 * <tc-description-list direction="row">
 *   <tc-description-term>Country</tc-description-term>
 *   <tc-description-details>Australia</tc-description-details>
 *
 *   <tc-description-term>Employer</tc-description-term>
 *   <tc-description-details>Blue Mountains Highway Motel</tc-description-details>
 * </tc-description-list>
 * ```
 */

@Component({
  selector: 'tc-description-term',
  templateUrl: './description-term.component.html',
  styleUrls: ['./description-term.component.scss']
})
export class DescriptionTermComponent {

}
