import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Industry} from "../model/industry";
import {Observable} from "rxjs";
import {EducationType} from "../model/education-type";

@Injectable({
  providedIn: 'root'
})
export class EducationTypeService {

  private apiUrl: string = environment.apiUrl + '/education-type';

  constructor(private http: HttpClient) { }

  listEducationTypes(): Observable<EducationType[]> {
    return this.http.get<EducationType[]>(`${this.apiUrl}`);
  }

}
