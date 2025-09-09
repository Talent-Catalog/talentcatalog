import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {MyFirstFormData, MySecondFormData} from "../model/form";
import {MyFirstFormComponent} from "../components/form/my-first-form/my-first-form.component";
import {MySecondFormComponent} from "../components/form/my-second-form/my-second-form.component";

@Injectable({
  providedIn: 'root'
})
export class CandidateFormService {

  // You need to add an entry to this map for each form that can be displayed in a FormTask.
  // The mapping is from the name of the form to an Angular component.
  private componentMap: Record<string, any> = {
    'MyFirstForm': MyFirstFormComponent,
    'MySecondForm': MySecondFormComponent
  }

  apiUrl: string = environment.apiUrl + '/form';
  halApiUrl: string = environment.halApiUrl;

  constructor(private http: HttpClient) { }

  createOrUpdateMyFirstForm(request: MyFirstFormData): Observable<MyFirstFormData> {
    return this.http.post<MyFirstFormData>(`${this.apiUrl}/my-first-form`, request);
  }

  getMyFirstForm(): Observable<MyFirstFormData> {
    return this.http.get<MyFirstFormData>(`${this.apiUrl}/my-first-form`);
  }

  createOrUpdateMySecondForm(candidateNumber: string, request: MySecondFormData): Observable<MySecondFormData> {
    return this.http.put<MySecondFormData>(`${this.halApiUrl}/my-second-form/${candidateNumber}`, request);
  }

  getMySecondForm(candidateNumber: string): Observable<MySecondFormData> {
    return this.http.get<MySecondFormData>(`${this.halApiUrl}/my-second-form/${candidateNumber}`);
  }

  getFormComponentByName(formName: string): any {
    return this.componentMap[formName];
  }
}
