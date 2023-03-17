import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Country} from "../model/country";

export interface VisaPathway {
  name: string;
  description: string;
  country: Country;
}

@Injectable({
  providedIn: 'root'
})
export class VisaPathwayService {

  private apiUrl: string = environment.apiUrl + '/visa-pathway';

  constructor(private http: HttpClient) { }

  listVisaPathwaysAU(): Observable<VisaPathway[]> {
    return this.http.get<VisaPathway[]>(`${this.apiUrl}/AU`);
  }

}
