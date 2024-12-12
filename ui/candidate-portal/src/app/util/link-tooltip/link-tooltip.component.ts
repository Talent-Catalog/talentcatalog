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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

/**
 * Provides a tooltip displaying and enabling edit and removal of properties of link-formatted text.
 */
@Component({
  selector: 'app-link-tooltip',
  templateUrl: './link-tooltip.component.html',
  styleUrls: ['./link-tooltip.component.scss']
})
export class LinkTooltipComponent implements OnInit {

  @Input() leftOffset: number;
  @Input() bottomOffset: number;
  @Input() url: string;

  @Output() editClicked = new EventEmitter<string>();
  @Output() removeClicked = new EventEmitter<string>();

  constructor() { }

  ngOnInit(): void {
  }

  public onEditClick() {
    this.editClicked.emit(this.url)
  }

  public onRemoveClick() {
    this.removeClicked.emit(this.url)
  }

}
