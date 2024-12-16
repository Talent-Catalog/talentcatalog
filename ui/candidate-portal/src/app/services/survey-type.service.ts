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

import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SurveyType} from "../model/survey-type";

@Injectable({
  providedIn: 'root'
})
export class SurveyTypeService {

  private apiUrl: string = environment.apiUrl + '/survey-type';

  constructor(private http: HttpClient) { }

  listActiveSurveyTypes(): Observable<SurveyType[]> {
    return this.http.get<SurveyType[]>(`${this.apiUrl}`);
  }

}
