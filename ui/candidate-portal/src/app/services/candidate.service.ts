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

  updateCandidateContactInfo(request): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/contact/email`, request);
  }

  /* Contact - alternate */
  getCandidateAlternateContacts(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/contact/alternate`);
  }

  updateCandidateAlternateContacts(request): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/contact/alternate`, request);
  }

  getCandidateAdditionalContacts(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/contact/additional`);
  }

  updateCandidateAdditionalContacts(request): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/contact/additional`, request);
  }

  /* Candidate Personal */
   getCandidatePersonal(): Observable<Candidate> {
      return this.http.get<Candidate>(`${this.apiUrl}/personal`);
   }

   updateCandidatePersonal(request): Observable<Candidate> {
      return this.http.post<Candidate>(`${this.apiUrl}/personal`, request);
   }


  // TODO create a get request and save request for each step of the registration process
  // TODO Each get request should only return the data needed to be displayed on the current registration page

  getCandidateProfessions(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/profession`);
  }

}
