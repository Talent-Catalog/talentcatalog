import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Industry} from "../model/industry";
import {Observable} from "rxjs";
import {Occupation} from "../model/occupation";

@Injectable({
  providedIn: 'root'
})
export class OccupationService {

  private apiUrl: string = environment.apiUrl + '/occupation';

  constructor(private http: HttpClient) { }

  listOccupations(): Observable<Occupation[]> {
    return this.http.get<Occupation[]>(`${this.apiUrl}`);
  }

}
