import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {Observable} from 'rxjs';
import {Candidate} from '../model/candidate';

@Injectable({
  providedIn: 'root'
})
export class CandidateService {

  apiUrl: string = environment.apiUrl + '/candidate';

  constructor(private http: HttpClient) {
  }

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

  /* Candidate Education Level*/
  updateCandidateEducationLevel(request): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/education`, request);
  }

  /* Candidate Additional Info */
  getCandidateAdditionalInfo(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/additional-info`);
  }

  updateCandidateAdditionalInfo(request): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/additional-info`, request);
  }

  getCandidateCandidateOccupations(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/candidateOccupation`);
  }

  getCandidateEducation(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/education`);
  }

  getCandidateLanguages(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/languages`);
  }

  getCandidateJobExperiences(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/job-experiences`);
  }

  getCandidateCertifications(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/certifications`);
  }


}
