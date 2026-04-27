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
import {Candidate} from '../../../../../model/candidate';
import {ServiceProvider} from '../../../../../model/services';
import {TaskAssignment} from "../../../../../model/task-assignment";
import {Observable} from "rxjs";
import {environment} from "../../../../../../environments/environment";

@Component({
    selector: 'app-services',
    templateUrl: './services.component.html',
    styleUrls: ['./services.component.scss']
})
export class ServicesComponent {
  protected readonly Service = ServiceProvider;
  selectedService: ServiceProvider;
  error;
  loading;
  @Input() candidate: Candidate;
  @Input() activeDuolingoTask: TaskAssignment;
  @Input() showLinkedin$: Observable<boolean>;
  @Input() showReference$: Observable<boolean>;
  @Input() showUnhcr$: Observable<boolean>;
  @Output() refresh = new EventEmitter();

  constructor() { }

  selectService(service: ServiceProvider) {
      this.selectedService = service;
  }

  onBackButtonClick(): void {
      this.selectedService = null;
      this.refresh.emit();
  }

  isLocalEnv(): boolean {
    return environment.environmentName === 'local';
  }

}
