import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Country} from "../model/country";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CountryService {

  private apiUrl: string = environment.apiUrl + '/country';

  constructor(private http: HttpClient) { }

  listCountries(): Observable<Country[]> {
    return this.http.get<Country[]>(`${this.apiUrl}`);
  }

}
