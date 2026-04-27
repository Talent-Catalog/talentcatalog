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
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-view-job-suggested-list',
  templateUrl: './view-job-suggested-list.component.html',
  styleUrls: ['./view-job-suggested-list.component.scss']
})
export class ViewJobSuggestedListComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }

}
