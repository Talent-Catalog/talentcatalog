import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Nationality} from "../model/nationality";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class NationalityService {

  private apiUrl: string = environment.apiUrl + '/nationality';

  constructor(private http: HttpClient) { }

  listNationalities(): Observable<Nationality[]> {
    return this.http.get<Nationality[]>(`${this.apiUrl}`);
  }

}
