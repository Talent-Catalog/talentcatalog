import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SkillName} from "../model/skill";

export interface ExtractSkillsRequest {
  lang: string;
  text: string;
}

@Injectable({
  providedIn: 'root'
})
export class SkillsService {
  apiUrl: string = environment.publicApiUrl + '/skill';

  constructor(private http: HttpClient) { }

  extractSkills(request: ExtractSkillsRequest): Observable<SkillName[]> {
    return this.http.post<SkillName[]>(`${this.apiUrl}/extract_skills`, request);
  }

}
