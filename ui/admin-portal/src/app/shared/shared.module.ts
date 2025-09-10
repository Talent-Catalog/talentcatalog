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
import {ButtonComponent} from './components/button/button.component';
import {NgbAlert, NgbNavModule, NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {TcTableComponent} from "./components/table/tc-table.component";
import {TcPaginationComponent} from './components/pagination/tc-pagination.component';
import {TcTabsComponent} from './components/tabs/tc-tabs.component';
import {TcTabComponent} from './components/tabs/tab/tc-tab.component';

import {TcTabHeaderComponent} from "./components/tabs/tab/header/tc-tab-header.component";
import {TcTabContentComponent} from './components/tabs/tab/content/tc-tab-content.component';

import {InputComponent} from './components/input/input.component';
import {FieldsetComponent} from './components/fieldset/fieldset.component';
import {FieldComponent} from './components/fieldset/field/field.component';
import {LabelComponent} from './components/fieldset/label/label.component';
import {DescriptionComponent} from './components/fieldset/description/description.component';
import {ErrorMessageComponent} from './components/fieldset/error-message/error-message.component';
import {TextareaComponent} from './components/textarea/textarea.component';
import {BadgeComponent} from './components/badge/badge.component';

import {DescriptionListComponent} from './components/description-list/description-list.component';
import {
  DescriptionDetailsComponent
} from './components/description-list/description-details/description-details.component';
import {
  DescriptionTermComponent
} from './components/description-list/description-term/description-term.component';
import {TcModalComponent} from './components/modal/tc-modal.component';
import {AlertComponent} from './components/alert/alert.component';

@NgModule({
  declarations: [
    ButtonComponent,
    TcTableComponent,
    TcPaginationComponent,
    InputComponent,
    FieldsetComponent,
    FieldComponent,
    LabelComponent,
    DescriptionComponent,
    ErrorMessageComponent,
    TextareaComponent,
    TcTabsComponent,
    TcTabComponent,
    TcTabHeaderComponent,
    TcTabContentComponent,
    BadgeComponent,
    DescriptionListComponent,
    DescriptionDetailsComponent,
    DescriptionTermComponent,
    TcModalComponent,
    AlertComponent
  ],
  imports: [
    CommonModule,
    NgbPaginationModule,
    NgbNavModule,
    NgbAlert
  ],
  exports: [
    ButtonComponent,
    TcTableComponent,
    TcPaginationComponent,
    InputComponent,
    FieldComponent,
    FieldsetComponent,
    LabelComponent,
    DescriptionComponent,
    ErrorMessageComponent,
    TextareaComponent,
    TcTabsComponent,
    TcTabComponent,
    TcTabHeaderComponent,
    TcTabContentComponent,
    BadgeComponent,
    DescriptionListComponent,
    DescriptionDetailsComponent,
    DescriptionTermComponent,
    TcModalComponent,
    AlertComponent
  ]
})
export class SharedModule { }
