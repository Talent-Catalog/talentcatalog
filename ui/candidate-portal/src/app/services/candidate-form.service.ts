import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ItalyCandidateTravelDocFormData, MyFirstFormData} from "../model/form";
import {MyFirstFormComponent} from "../components/form/my-first-form/my-first-form.component";
import {
  ItalyCandidateTravelDocumentFormComponent
} from "../components/form/italy-travel-document-form/italy-candidate-travel-document-form.component";

@Injectable({
  providedIn: 'root'
})
export class CandidateFormService {

  // You need to add an entry to this map for each form that can be displayed in a FormTask.
  // The mapping is from the name of the form to an Angular component.
  private componentMap: Record<string, any> = {
    'MyFirstForm': MyFirstFormComponent,
    'ItalyCandidateTravelDocumentForm': ItalyCandidateTravelDocumentFormComponent
  }

  apiUrl: string = environment.apiUrl + '/form';

  constructor(private http: HttpClient) { }

  createOrUpdateMyFirstForm(request: MyFirstFormData): Observable<MyFirstFormData> {
    return this.http.post<MyFirstFormData>(`${this.apiUrl}/my-first-form`, request);
  }

  getMyFirstForm(): Observable<MyFirstFormData> {
    return this.http.get<MyFirstFormData>(`${this.apiUrl}/my-first-form`);
  }

  createOrUpdateTravelDocumentForm(request: ItalyCandidateTravelDocFormData): Observable<ItalyCandidateTravelDocFormData> {
    return this.http.post<ItalyCandidateTravelDocFormData>(`${this.apiUrl}/travel-doc-form`, request);
  }

  getTravelDocumentForm(): Observable<ItalyCandidateTravelDocFormData> {
    return this.http.get<ItalyCandidateTravelDocFormData>(`${this.apiUrl}/travel-doc-form`);
  }

  getFormComponentByName(formName: string): any {
    return this.componentMap[formName];
  }
}
