import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Job, JobDocType, SearchJobRequest, UpdateJobRequest} from "../model/job";
import {SearchResults} from "../model/search-results";
import {UpdateLinkRequest} from "../components/util/input/input-link/input-link.component";
import {IntakeService} from "../components/util/intake/IntakeService";

@Injectable({
  providedIn: 'root'
})
export class JobService implements IntakeService {

  private apiUrl: string = environment.apiUrl + '/job';

  constructor(private http: HttpClient) { }


  create(request: UpdateJobRequest): Observable<Job> {
    return this.http.post<Job>(`${this.apiUrl}`, request);
  }

  createSuggestedSearch(id: number, suffix: string): Observable<Job> {
    return this.http.post<Job>(`${this.apiUrl}/${id}/create-search`, suffix);
  }

  get(id: number): Observable<Job> {
    return this.http.get<Job>(`${this.apiUrl}/${id}`);
  }

  publishJob(id: number): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/publish`, null);
  }

  removeSuggestedSearch(id: number, savedSearchId: number): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/remove-search`, savedSearchId);
  }

  searchPaged(request: SearchJobRequest): Observable<SearchResults<Job>> {
    return this.http.post<SearchResults<Job>>(`${this.apiUrl}/search-paged`, request);
  }

  update(id: number, request: UpdateJobRequest): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}`, request);
  }

  updateJobLink(id: number, docType: JobDocType, updateLinkRequest: UpdateLinkRequest): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/${docType}link`, updateLinkRequest);
  }

  //todo With this approach Job needs to return its users - and that is what is used to display star
  //However on server, it is bidirectional, so that user has jobs. This makes JobSpecification code easier
  //Bidirectional means that User and SalesforceJob entities update each other
  updateStarred(id: number, starred: boolean): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/starred`, starred);
  }

  updateSummary(id: number, summary: string): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/summary`, summary);
  }

  uploadJobDoc(id: number, docType: JobDocType, formData: FormData): Observable<Job> {
    return this.http.post<Job>(
      `${this.apiUrl}/${id}/upload/${docType}`, formData);

  }

  updateIntakeData(id: number, formData: Object): Observable<void> {
    //todo
    return undefined;
  }

}
