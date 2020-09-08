import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {Observable} from 'rxjs';
import {Candidate} from '../model/candidate';
import {map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CandidateService {

  apiUrl: string = environment.apiUrl + '/candidate';

  constructor(private http: HttpClient) {
  }

  /* Contact */
  getCandidateContact(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/contact`);
  }

  updateCandidateContact(request): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/contact`, request);
  }

  /* Personal */
  getCandidatePersonal(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/personal`);
  }

  updateCandidatePersonal(request): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/personal`, request);
  }

  /* Education */
  getCandidateEducation(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/education`);
  }

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

  /* Candidate Survey */
  getCandidateSurvey(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/survey`);
  }

  updateCandidateSurvey(request): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/survey`, request);
  }

  getCandidateCandidateOccupations(): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/occupation`);
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

  updateCandidateCertification(request): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/certifications`, request);
  }

  getStatus() {
    return this.http.get<Candidate>(`${this.apiUrl}/status`);
  }

  getProfile() {
    return this.http.get<Candidate>(`${this.apiUrl}/profile`);
  }

  getCandidateNumber() {
    return this.http.get<Candidate>(`${this.apiUrl}/candidate-number`);
  }

  downloadCv() {
    return this.http.get(`${this.apiUrl}/cv.pdf`, {responseType: 'blob'}).pipe(map(res => {
      return new Blob([res], { type: 'application/pdf', });
    }));
  }
}
