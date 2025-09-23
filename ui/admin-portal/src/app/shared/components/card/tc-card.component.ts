import {Component} from '@angular/core';

/**
 * @component CardComponent
 * @selector tc-card
 * @description
 * A reusable container component for grouping related content.
 * Provides a consistent card layout with optional header and
 * body areas, rounded corners, and shadow styling.
 *
 * **Features**
 * - Wraps projected content in a styled card container
 * - Supports a dedicated `<tc-card-header>` slot for the header area
 * - Applies consistent padding and spacing to the card body
 * - Rounded corners and shadow for visual hierarchy
 * - Minimal, flexible design for use across multiple contexts
 *
 * **Structure**
 * - `<tc-card>`: main wrapper
 *   - `<tc-card-header>` (optional): header section
 *   - Card body content (default `<ng-content>`)
 *
 * @example
 * ```html
 * <!-- Basic card with content -->
 * <tc-card>
 *   <p>This is a card body</p>
 * </tc-card>
 *
 * <!-- Card with header and body -->
 * <tc-card>
 *   <tc-card-header>
 *     Contact Information
 *   </tc-card-header>
 *   <div class="row">
 *     <div class="col-md-4">Gender: Female</div>
 *     <div class="col-md-4">D.O.B.: 12/12/2001</div>
 *   </div>
 * </tc-card>
 * ```
 */

@Component({
  selector: 'tc-card',
  templateUrl: './tc-card.component.html',
  styleUrls: ['./tc-card.component.scss']
})
export class TcCardComponent {

}
