/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-agreement-content',
  templateUrl: './agreement-content.component.html',
  styleUrls: ['./agreement-content.component.scss']
})
export class AgreementContentComponent {
  @Input() content: string;
  @Input() counterpartyName: string;
  @Input() acceptedId: string;
  @Input() acceptedDate: string;
  /** Emitted when the Back button is clicked. If no observer is bound, the button is hidden. */
  @Output() back = new EventEmitter<void>();

  get showBack(): boolean {
    return this.back.observers.length > 0;
  }
}
