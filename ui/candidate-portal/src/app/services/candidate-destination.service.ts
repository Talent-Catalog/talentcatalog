import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateDestination} from "../model/candidate";

export interface CreateCandidateDestinationRequest {
  countryId: number,
  interest: string,
  notes?: string
}

export interface UpdateCandidateDestinationRequest {
  interest: string,
  notes?: string
}

@Injectable({
  providedIn: 'root'
})
export class CandidateDestinationService {
  private apiUrl: string = environment.apiUrl + '/candidate-destination';

  constructor(private http: HttpClient) {
  }

  create(candidateId: number, request: CreateCandidateDestinationRequest): Observable<CandidateDestination> {
    return this.http.post<CandidateDestination>(`${this.apiUrl}/${candidateId}`, request);
  }

  update(id: number, request: UpdateCandidateDestinationRequest): Observable<CandidateDestination>  {
    return this.http.put<CandidateDestination>(`${this.apiUrl}/${id}`, request);
  }
}
