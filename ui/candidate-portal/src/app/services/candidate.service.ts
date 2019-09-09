import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {Candidate} from "../model/candidate";

@Injectable({
  providedIn: 'root'
})
export class CandidateService {

  apiUrl: string = environment.apiUrl + '/candidate';

  constructor(private http: HttpClient) { }

  /* Contact */
  getCandidateContactInfo(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/contact/email`);
  }

  saveCandidateContactInfo(request) {
    return this.http.post(`${this.apiUrl}/contact/email`, request);
  }

  /* Contact - alternate */
  getCandidateAlternateContacts(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/contact/alternate`);
  }

  saveCandidateAlternateContacts(request) {
    return this.http.post(`${this.apiUrl}/contact/alternate`, request);
  }

  // TODO create a get request and save request for each step of the registration process
  // TODO Each get request should only return the data needed to be displayed on the current registration page

}
