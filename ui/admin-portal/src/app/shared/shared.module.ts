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
import { InputComponent } from './components/input/input.component';
import { FieldsetComponent } from './components/fieldset/fieldset.component';
import { FieldComponent } from './components/fieldset/field/field.component';
import { LabelComponent } from './components/fieldset/label/label.component';
import { DescriptionComponent } from './components/fieldset/description/description.component';
import { ErrorMessageComponent } from './components/fieldset/error-message/error-message.component';

@NgModule({
  declarations: [ButtonComponent, InputComponent, FieldsetComponent, FieldComponent, LabelComponent, DescriptionComponent, ErrorMessageComponent],
  imports: [
    CommonModule,
  ],
  exports: [
    ButtonComponent,
    InputComponent,
    FieldComponent,
    FieldsetComponent, LabelComponent, DescriptionComponent, ErrorMessageComponent
  ]
})
export class SharedModule {
}
