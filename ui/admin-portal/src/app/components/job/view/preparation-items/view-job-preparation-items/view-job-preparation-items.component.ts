/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {JobPrepItem} from "../../../../../model/job-prep-item";

@Component({
  selector: 'app-view-job-preparation-items',
  templateUrl: './view-job-preparation-items.component.html',
  styleUrls: ['./view-job-preparation-items.component.scss']
})
export class ViewJobPreparationItemsComponent implements OnInit {
  @Input() jobPrepItems: JobPrepItem[];
  @Input() editable: boolean;
  @Output() itemSelected = new EventEmitter();
  @Output() onPublish = new EventEmitter();

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

  publish() {
    this.onPublish.emit();
  }
}
