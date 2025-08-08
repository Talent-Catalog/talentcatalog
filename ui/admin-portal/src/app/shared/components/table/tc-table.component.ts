import {Component, EventEmitter, Input, Output, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'tc-table',
  templateUrl: './tc-table.component.html',
  styleUrls: ['./tc-table.component.scss'],
  // This means the tc-table scss style only applies to <table> elements within the tc-table component
  encapsulation: ViewEncapsulation.None
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
