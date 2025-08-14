import {Component, EventEmitter, Input, Output, ViewEncapsulation} from '@angular/core';

/**
 * @component TcTableComponent
 * @description
 * A reusable table wrapper component that contains both the table and pagination UI.
 *
 * **Features:**
 * - Displays and styles a data table
 * - Integrates pagination controls
 * - Wraps the `<thead>` and `<tbody>` markup, allowing customization
 *   (e.g., icons, popovers, chat read statuses)
 *
 * @selector tc-table
 *
 * @example
 * ```html
 * <tc-table
 *   name="My Paged Search Results"
 *   [totalElements]="results?.totalElements"
 *   [pageSize]="pageSize"
 *   [(pageNumber)]="pageNumber"
 *   (pageChange)="search()">
 * </tc-table>
 * ```
 */
@Component({
  selector: 'tc-table',
  templateUrl: './tc-table.component.html',
  styleUrls: ['./tc-table.component.scss'],
  // Setting to None means the tc-table scss style only applies styles to <table> elements within the tc-table component,
  // avoids styles bleeding out to the other <table> components not wrapped by <tc-table>. Also overrides any
  // existing <table> styles from bootstrap.
  encapsulation: ViewEncapsulation.None
})
export class TcTableComponent {
  /** Table name (optional) */
  @Input() name: string;
  /** Type of table styles (default is Basic) */
  @Input() type: 'Basic' | 'Striped' | 'Dropdown' = 'Basic';

  /** Variables for pagination (see {@link TcPaginationComponent} for doc) */
  @Input() totalElements: number;
  @Input() pageSize: number;
  @Input() pageNumber: number;
  @Output() pageNumberChange = new EventEmitter<number>();
  @Output() pageChange = new EventEmitter();

  onPageChange(newPageNumber: number) {
    this.pageNumber = newPageNumber;
    // Emit the pageNumberChange so I can use two way binding on pageNumber
    this.pageNumberChange.emit(newPageNumber);
    // Emit the pageChange event so I can do an action on the page change e.g. Search
    this.pageChange.emit();
  }

}
