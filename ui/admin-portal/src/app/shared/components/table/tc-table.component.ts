import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-tc-table',
  templateUrl: './tc-table.component.html',
  styleUrls: ['./tc-table.component.scss']
})
export class TcTableComponent {

  @Input() name: string;
  @Input() type: 'Basic' | 'Striped' | 'Dropdown' = 'Basic';
  // Variables for pagination
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
