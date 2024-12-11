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
import {UntypedFormGroup} from "@angular/forms";
import {TaskAssignment} from "../../../../../../../model/task-assignment";

@Component({
  selector: 'app-view-question-task',
  templateUrl: './view-question-task.component.html',
  styleUrls: ['./view-question-task.component.scss']
})
export class ViewQuestionTaskComponent implements OnInit {
  @Input() form: UntypedFormGroup;
  @Input() selectedTask: TaskAssignment;

  constructor() { }

  ngOnInit(): void {
  }

}
