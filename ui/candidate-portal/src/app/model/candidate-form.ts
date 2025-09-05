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

/**
 * Defines what a Candidate Form component should look like.
 *
 * It should have an <code>@Input readOnly: boolean</code> which makes the form read only and
 * disables submitting the form
 *
 * It should also have an <code>@Output submitted = new EventEmitter<form data> </code> which
 * emits an event containing the form's data when it is submitted.
 */
export interface ICandidateFormComponent<T = unknown> {
  readOnly: boolean;
  submitted: import("@angular/core").EventEmitter<T>;
}

