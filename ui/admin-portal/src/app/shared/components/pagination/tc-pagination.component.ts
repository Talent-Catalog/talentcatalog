import {Component, EventEmitter, Input, Output} from '@angular/core';

export interface PageInfo {
  pageSize: number;
  pageNumber: number;
}

@Component({
  selector: 'app-tc-pagination',
  templateUrl: './tc-pagination.component.html',
  styleUrls: ['./tc-pagination.component.scss']
})
export class TcPaginationComponent {
  @Input() totalElements: number;
  @Input() pageNumber: number;
  @Input() pageSize: number;
  @Output() pageChange = new EventEmitter<number>();

  onPageChange(newPageNumber: number) {
    this.pageNumber = newPageNumber
    this.pageChange.emit(newPageNumber);
  }

  // See NGB Pagination doc for this custom example: https://ng-bootstrap.github.io/#/components/pagination/examples#customization
  selectPage(page: string) {
    this.pageNumber = parseInt(page, 10) || 1;
  }
  formatInput(input: HTMLInputElement) {
    const FILTER_PAG_REGEX = /[^0-9]/g;
    input.value = input.value.replace(FILTER_PAG_REGEX, '');
  }

}
