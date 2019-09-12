import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Industry} from "../model/industry";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class IndustryService {

  private apiUrl: string = environment.apiUrl + '/industry';

  constructor(private http: HttpClient) { }

  listIndustries(): Observable<Industry[]> {
    return this.http.get<Industry[]>(`${this.apiUrl}`);
  }

}
