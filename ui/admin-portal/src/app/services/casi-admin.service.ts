import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";


/**
 * Service for managing CASI inventory and assignments in the admin portal.
 * Provides methods to import inventory, count available items, and assign items to candidates or lists.
 *
 * @author sadatmalik
 */
@Injectable({
  providedIn: 'root'
})
export class CasiAdminService {
  private apiBaseUrl = environment.apiUrl + '/services';

  constructor(private http: HttpClient) { }

  importInventory(provider: string, serviceCode: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<any>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/import`,
      formData,
      {headers: new HttpHeaders({ 'enctype': 'multipart/form-data' })}
    );
  }

  countAvailable(provider: string, serviceCode: string): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/available/count`
    );
  }

  assignToCandidate(provider: string, serviceCode: string, candidateId: number): Observable<any> {
    return this.http.post<any>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/assign/candidate/${candidateId}`,
      null
    );
  }

  assignToList(provider: string, serviceCode: string, listId: number): Observable<any[]> {
    return this.http.post<any[]>(
      `${this.apiBaseUrl}/${provider}/${serviceCode}/assign/list/${listId}`,
      null
    );
  }
}
