import {Component, EventEmitter, Input, Output} from '@angular/core';
import {PageInfo} from "../pagination/tc-pagination.component";
import {SearchResults} from "../../../model/search-results";

export type TableType = 'Basic' | 'Striped' | 'Dropdown';

@Component({
  selector: 'app-display-table',
  templateUrl: './display-table.component.html',
  styleUrls: ['./display-table.component.scss']
})
export class DisplayTableComponent {

  @Input() name: string;
  @Input() columns: string[] = ["Name", "Stage", "Created", "Due"];
  @Input() searchResults: SearchResults<any>;
  @Input() type: TableType = 'Basic';
  // Variables for pagination
  @Input() pageNumber;
  @Output() pageChange = new EventEmitter<PageInfo>();

  onPageChange(pageInfo: PageInfo) {
    this.pageChange.emit(pageInfo);
  }

}
