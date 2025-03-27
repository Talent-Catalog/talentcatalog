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
import {TaskAssignment} from "../../../../../model/task-assignment";

@Component({
    selector: 'app-services',
    templateUrl: './services.component.html',
    styleUrls: ['./services.component.scss']
})
export class ServicesComponent {

    selectedService: String;
    error;
    loading;
    @Input() candidate: Candidate;
    @Input() activeDuolingoTask: TaskAssignment;
    @Output() refresh = new EventEmitter();

    constructor() {
    }

    selectService(serviceName: String) {
        this.selectedService = serviceName;
    }

    unSelectService() {
        this.selectedService = null;
        this.refresh.emit();
    }
}
