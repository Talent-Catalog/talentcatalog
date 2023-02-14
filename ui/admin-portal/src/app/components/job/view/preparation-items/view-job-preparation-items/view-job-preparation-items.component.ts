import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {JobPrepItem} from "../../../../../model/job-prep-item";

@Component({
  selector: 'app-view-job-preparation-items',
  templateUrl: './view-job-preparation-items.component.html',
  styleUrls: ['./view-job-preparation-items.component.scss']
})
export class ViewJobPreparationItemsComponent implements OnInit {
  @Input() jobPrepItems: JobPrepItem[];
  @Output() itemSelected = new EventEmitter();

  selectedItem: JobPrepItem;
  error: any;

  ngOnInit(): void {
  }

  onItemSelected(item: JobPrepItem) {
    this.selectedItem = item;

    this.itemSelected.emit(item);
  }

  isSelected(item: JobPrepItem): boolean {
    let res = item === this.selectedItem;
    return res;
  }
}
