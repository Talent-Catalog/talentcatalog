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
import {Injectable} from "@angular/core";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {CandidateOpportunity} from "../model/candidate-opportunity";

@Injectable({
  providedIn: 'root'
})
export class CandidateOpportunityService {

  private mocking = true;
  private mockOpp: CandidateOpportunity = {
    id: 234,
    name: 'Fred(12345)-Test opp',
    jobOpp: {
      id: 123,
      name: "Test job"
    },

    //Internally enums are stored by their keys. Override typing error. This is how Json maps
    //to Typescript enums
    // todo: I think this going to cause problems with getCandidateOpportunityStageName not always working
    //Maybe it needs to be smarter and be able to check wether it is being passed a string or an enum - or also looks for StringValues
    // @ts-ignore
    stage: "prospect",
    nextStep: "Come on get this guy hired",
    employerFeedback: "English is not great"
  }
  private apiUrl: string = environment.apiUrl + '/opp';

  constructor(private http: HttpClient) { }

  get(id: number): Observable<CandidateOpportunity> {
    let observable;
    if (this.mocking) {
      observable = of(this.mockOpp);
    } else {
      observable = this.http.get<CandidateOpportunity>(`${this.apiUrl}/${id}`);
    }
    return observable;
  }

}
