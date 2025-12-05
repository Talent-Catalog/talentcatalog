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

import {Component} from '@angular/core';

/**
 * @component CardHeaderComponent
 * @selector tc-card-header
 * @description
 * A flexible header section for use inside `<tc-card>`.
 * Accepts projected content such as titles, buttons, or actions.
 * Typically used at the top of a card to provide a heading and controls.
 *
 * **Features**
 * - Consistent header styling with background color and padding
 * - Flexible layout: left-aligned title, right-aligned actions
 * - Rounded top corners to visually connect with the card container
 * - Uses `<ng-content>` so you can project any custom markup
 * - Renders a wrapping `<div>` with the `.tc-card-header` class to apply Talent Catalog's card-header styling.
 *
 * **Usage**
 * Must be used inside a `<tc-card>`. Not intended for standalone use.
 *
 * @example
 * ```html
 * <!-- Header -->
 * <tc-card>
 *   <tc-card-header>
 *     Registration
 *   </tc-card-header>
 *   <p>Body content here</p>
 * </tc-card>
 *
 * ```
 */

@Component({
  selector: 'tc-card-header',
  templateUrl: './tc-card-header.component.html',
  styleUrls: ['./tc-card-header.component.scss']
})
export class TcCardHeaderComponent {

}
