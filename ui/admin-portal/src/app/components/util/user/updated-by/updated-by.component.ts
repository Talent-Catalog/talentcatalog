/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';

@Component ({
  selector: 'app-updated-by',
  templateUrl: './updated-by.component.html',
  styleUrls: ['./updated-by.component.scss']
})
export class UpdatedByComponent implements OnInit, OnChanges {

  @Input() object: {[key: string]: any};

  constructor() {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Replace the form value with the new candidates context notes when
    //changing from one candidate to the next or when selection has changed.
    if (changes.object.currentValue.updatedDate && changes.object.currentValue?.updatedDate !== changes.object.previousValue?.updatedDate) {
      console.log(this.object);
    }
  }

}
