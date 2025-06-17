/*
 * Copyright (c) 2025 Talent Catalog.
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

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  NgbPagination,
  NgbPaginationFirst,
  NgbPaginationLast,
  NgbPaginationNext,
  NgbPaginationNumber,
  NgbPaginationPages,
  NgbPaginationPrevious
} from "@ng-bootstrap/ng-bootstrap";
import {DisplayTableComponent} from "./components/table/display-table.component";
import {TcPaginationComponent} from './components/pagination/tc-pagination.component';


@NgModule({
  declarations: [DisplayTableComponent, TcPaginationComponent],
  imports: [
    CommonModule,
    NgbPagination,
    NgbPaginationPrevious,
    NgbPaginationNext,
    NgbPaginationNumber,
    NgbPaginationPages,
    NgbPaginationFirst,
    NgbPaginationLast
  ],
  exports: [
    DisplayTableComponent, TcPaginationComponent
  ]
})
export class SharedModule { }
