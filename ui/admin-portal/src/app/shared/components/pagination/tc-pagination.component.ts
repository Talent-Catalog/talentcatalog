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
  @Input() collectionSize: number;
  @Input() pageNumber: number;
  @Input() pageSize = 2;
  @Output() pageChange = new EventEmitter<PageInfo>();

  onPageChange(page: number) {
    let pageRequest: PageInfo = {
      pageNumber: page,
      pageSize: this.pageSize
    }
    this.pageChange.emit(pageRequest);
  }

}
