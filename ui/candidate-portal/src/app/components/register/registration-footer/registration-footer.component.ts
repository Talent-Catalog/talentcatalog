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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {RegistrationService} from "../../../services/registration.service";

@Component({
  selector: 'app-registration-footer',
  templateUrl: './registration-footer.component.html',
  styleUrls: ['./registration-footer.component.scss']
})
export class RegistrationFooterComponent {

  @Input() nextDisabled: boolean = false;
  @Input() backDisabled: boolean = false;
  @Input() hideBack: boolean = false;
  @Input() hideNext: boolean = false;
  @Input() type: 'step' | 'submit' | 'update' = 'step';

  @Output() backClicked = new EventEmitter();
  @Output() nextClicked = new EventEmitter();

  constructor(public regoService: RegistrationService) { }

  back() {
    this.backClicked.emit();
  }

  next() {
    this.nextClicked.emit();
  }

}
