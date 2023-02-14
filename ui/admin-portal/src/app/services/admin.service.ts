import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl: string = environment.apiUrl + '/system';

  constructor(private http: HttpClient) { }

  call(apicall: string): Observable<void> {
    return this.http.get<void>(`${this.apiUrl}/${apicall}`);
  }

}
