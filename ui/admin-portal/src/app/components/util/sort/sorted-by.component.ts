import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-sorted-by',
  templateUrl: './sorted-by.component.html',
  styleUrls: ['./sorted-by.component.scss']
})
export class SortedByComponent implements OnInit {

  @Input() sortColumn: string;
  @Input() sortDirection: string;
  @Input() column: string;

  debugging: boolean = false;

  constructor() {
  }

  ngOnInit() {
    if (this.debugging) {
      console.log(this.sortColumn);
      console.log(this.sortDirection);
      console.log(this.column);
    }
  }

}
