import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Profession} from "../model/profession";

@Injectable({
  providedIn: 'root'
})
export class ProfessionService {

  private apiUrl: string = environment.apiUrl + '/profession';

  constructor(private http: HttpClient) { }

  createProfession(request): Observable<Profession> {
    return this.http.post<Profession>(`${this.apiUrl}`, request);
  }

  deleteProfession(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
