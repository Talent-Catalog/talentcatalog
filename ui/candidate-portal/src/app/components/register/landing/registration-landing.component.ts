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
import {RegistrationService} from '../../../services/registration.service';

@Component({
  selector: 'app-registration-landing',
  templateUrl: './registration-landing.component.html',
  styleUrls: ['./registration-landing.component.scss']
})

//todo this component is no longer used - can be removed. Replaced by squarespace website.
//NB: remember also to remove the translations, which are prone to cause confusion if retained.
export class RegistrationLandingComponent implements OnInit {

  constructor(private registrationService: RegistrationService) { }

  ngOnInit() {
  }

  next() {
    this.registrationService.next();
  }
}
