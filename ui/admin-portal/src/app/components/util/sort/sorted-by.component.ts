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
    // if (this.debugging) {
    //   console.log(this.sortColumn);
    //   console.log(this.sortDirection);
    //   console.log(this.column);
    // }
  }

}
