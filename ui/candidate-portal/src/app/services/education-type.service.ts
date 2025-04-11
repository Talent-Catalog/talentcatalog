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

import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Industry} from "../model/industry";
import {Observable} from "rxjs";
import {EducationType} from "../model/education-type";

@Injectable({
  providedIn: 'root'
})
export class EducationTypeService {

  private apiUrl: string = environment.apiUrl + '/education-type';

  constructor(private http: HttpClient) { }

  listEducationTypes(): Observable<EducationType[]> {
    return this.http.get<EducationType[]>(`${this.apiUrl}`);
  }

}
