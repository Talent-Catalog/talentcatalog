/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
import {Job, JobDocType, SearchJobRequest, UpdateJobRequest} from "../model/job";
import {SearchResults} from "../model/search-results";
import {UpdateLinkRequest} from "../components/util/input/input-link/input-link.component";
import {IntakeService} from "../components/util/intake/IntakeService";
import {JobOppIntake} from "../model/job-opp-intake";

@Injectable({
  providedIn: 'root'
})
export class JobOppIntakeService implements IntakeService {

  private apiUrl: string = environment.apiUrl + '/joi';

  constructor(private http: HttpClient) { }

  get(id: number): Observable<JobOppIntake> {
    return this.http.get<JobOppIntake>(`${this.apiUrl}/${id}`);
  }

  updateIntakeData(id: number, formData: Object): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/intake`, formData);
  }
}
