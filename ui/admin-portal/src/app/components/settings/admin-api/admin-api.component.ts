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

import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {AdminService} from "../../../services/admin.service";

@Component({
  selector: 'app-admin-api',
  templateUrl: './admin-api.component.html',
  styleUrls: ['./admin-api.component.scss']
})
export class AdminApiComponent implements OnInit {

  ack: string;
  error: any;
  form: UntypedFormGroup;

  constructor(
    private fb: UntypedFormBuilder,
    private adminService: AdminService
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      apicall: [],
    });
  }

  send() {
    if (this.form.value.apicall) {
      this.ack = null;
      this.error = null;
      this.adminService.call(this.form.value.apicall).subscribe(
        () => {this.ack = "Done"},
        (error) => {this.error = error}
      )
    }
  }
}
