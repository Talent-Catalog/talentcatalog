import {Component, EventEmitter, Input, Output, ViewEncapsulation} from '@angular/core';

/**
 * @component TcPaginationComponent
 * @description
 * A reusable pagination component that contains ngb pagination UI.
 *
 * **Features:**
 * - Displays and styles the pagination
 * - Output event on page change emitting new page number
 *
 * @selector tc-pagination
 *
 * @example
 * ```html
 *  <tc-pagination
 *    [totalElements]="totalElements"
 *    [pageSize]="pageSize"
 *    [pageNumber]="pageNumber"
 *    (pageChange)="onPageChange($event)">
 *   </tc-pagination>
 * ```
 */
@Component({
  selector: 'tc-pagination',
  templateUrl: './tc-pagination.component.html',
  styleUrls: ['./tc-pagination.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class TcPaginationComponent {
  /** Total number of results to be paged */
  @Input() totalElements: number;
  /** Page number that is selected */
  @Input() pageNumber: number;
  /** Number of results per page */
  @Input() pageSize: number;
  /** Output event fires on page change and emits the new page number */
  @Output() pageChange = new EventEmitter<number>();

  onPageChange(newPageNumber: number) {
    if (newPageNumber >= 1) {
      this.pageNumber = newPageNumber
      this.pageChange.emit(newPageNumber);
    }
  }

  // See NGB Pagination doc for this custom example: https://ng-bootstrap.github.io/#/components/pagination/examples#customization
  selectPage(page: string) {
    this.onPageChange(parseInt(page, 10) || 1);
  }

  formatInput(input: HTMLInputElement) {
    const FILTER_PAG_REGEX = /[^0-9]/g;
    input.value = input.value.replace(FILTER_PAG_REGEX, '');
  }

}
