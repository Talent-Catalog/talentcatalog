import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SurveyType} from "../model/survey-type";

@Injectable({
  providedIn: 'root'
})
export class SurveyTypeService {

  private apiUrl: string = environment.apiUrl + '/survey-type';

  constructor(private http: HttpClient) { }

  listSurveyTypes(): Observable<SurveyType[]> {
    return this.http.get<SurveyType[]>(`${this.apiUrl}`);
  }

}
