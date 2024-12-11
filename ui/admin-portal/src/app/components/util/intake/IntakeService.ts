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

import {Observable} from "rxjs";

export interface IntakeService {

  /**
   * Note that the sent data, formData, is not typed.
   * The data is copied across using the name of the form fields.
   * Those names must match field names in a corresponding IntakeDataUpdate.java object on the
   * server.
   * @param id ID of entity being updated from the intake data form
   * @param formData form.value of an intake data form.
   */
  updateIntakeData(id: number, formData: Object): Observable<void>;

}
