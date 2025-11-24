import {Component, EventEmitter, Input, Output, ViewEncapsulation} from '@angular/core';
import {SearchResults} from "../../../model/search-results";

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
 *   [results]="results"
 *   [loading]="loading"
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
  // Required inputs
  /** Knowing the results allows the table to display a 'no results' warning if no results appear
   * (e.g. in a table search).
   * For paged search results passing them in allows the table to include the pagination feature.
   */
  @Input() results: SearchResults<any> | any[];
  @Input() loading: boolean = false;

  // Optional style inputs
  @Input() name: string;
  @Input() striped: boolean = false;
  @Input() hover: boolean = true;
  // Optional if pagination supported
  @Input() paginationPosition: 'top' | 'bottom' = 'bottom';
  /** Pass false when rows are not intended to be clickable e.g. to display a summary card */
  @Input() clickableRows: boolean = true;

  // Required inputs IF results are paged to support pagination
  @Input() pageNumber: number;
  @Output() pageNumberChange = new EventEmitter<number>();
  @Output() pageChange = new EventEmitter();

  get classList() {
    return {
      'clickable-rows': this.clickableRows,
      'table-striped': this.striped,
      'table-hover': this.hover
    };
  }

  get totalElements(): number {
    if (!this.results) {
      return 0;
    }

    if (Array.isArray(this.results)) {
      return this.results.length;
    }

    return this.results.totalElements;
  }

  get pageSize(): number | null {
    if (!this.results || Array.isArray(this.results)) {
      return null;
    }

    return this.results.size;
  }

  get hasResults(): boolean {
    return !!this.results;
  }

  get noResults(): boolean {
    if (!this.results) {
      return false;
    }

    if (Array.isArray(this.results)) {
      return this.results.length === 0;
    }

    return this.results.totalElements === 0;
  }

  get isPagedResults(): boolean {
    if (!this.results) {
      return false;
    }

    return !Array.isArray(this.results);
  }

  onPageChange(newPageNumber: number) {
    this.pageNumber = newPageNumber;
    // Emit the pageNumberChange so I can use two way binding on pageNumber
    this.pageNumberChange.emit(newPageNumber);
    // Emit the pageChange event so I can do an action on the page change e.g. Search
    this.pageChange.emit();
  }

}
