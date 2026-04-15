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

import {Component, TemplateRef, ViewChild} from '@angular/core';

/**
 * @component TcTabHeaderComponent
 * @description
 * A wrapper component that contains the header content.
 *
 * **Features:**
 * - Contains the content to be displayed in the tab nav
 *
 * @selector tc-tab-header
 *
 * @example
 * ```html
 * <tc-tabs [activeTabId]="activeTabId" (tabChanged)="setActiveTab($event)">
 *   <tc-tab id="FirstTab" description="This is the first tab">
 *     <tc-tab-header>First Tab</tc-tab-header>
 *     <tc-tab-content>
 *       <app-component></app-component>
 *     </tc-tab-content>
 *   </tc-tab>
 *   <tc-tab id="SecondTab" description="This is the second tab">
 *     <tc-tab-header>Second Tab</tc-tab-header>
 *     <tc-tab-content>
 *       This is some content that isn't in a component.
 *     </tc-tab-content>
 *   </tc-tab>
 * </tc-tabs>
 * ```
 */
@Component({
  selector: 'tc-tab-header',
  templateUrl: './tc-tab-header.component.html',
  styleUrls: ['./tc-tab-header.component.scss']
})
export class TcTabHeaderComponent {
  @ViewChild(TemplateRef, { static: true }) template!: TemplateRef<any>;
}
