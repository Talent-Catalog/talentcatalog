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

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';
import {ButtonComponent} from './components/button/button.component';
import {TcIconComponent} from './components/icon-component/tc-icon.component';
import {TcAccordionComponent} from './components/accordion/tc-accordion.component';
import {TcAccordionItemComponent} from './components/accordion/accordion-item/tc-accordion-item.component';

@NgModule({
  declarations: [
    ButtonComponent,
    TcIconComponent,
    TcAccordionComponent,
    TcAccordionItemComponent
  ],
  imports: [
    CommonModule,
    RouterLink
  ],
  exports: [
    ButtonComponent,
    TcIconComponent,
    TcAccordionComponent,
    TcAccordionItemComponent
  ]
})
export class SharedModule { }

