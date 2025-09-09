import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {MyFirstForm, MyFirstFormUpdateRequest} from "../model/form";

@Injectable({
  providedIn: 'root'
})
export class CandidateFormService {
  apiUrl: string = environment.apiUrl + '/form';

  constructor(private http: HttpClient) { }

  createOrUpdateMyFirstForm(request: MyFirstFormUpdateRequest): Observable<MyFirstForm> {
    return this.http.post<MyFirstForm>(`${this.apiUrl}/my-first-form`, request);
  }

}
