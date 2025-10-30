import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {FamilyDocFormComponent} from "../components/form/family-doc-form/family-doc-form.component";
import {
  FamilyDocFormData,
  FamilyRsdEvidenceFormData,
  MyFirstFormData,
  MySecondFormData,
  TravelDocFormData
} from "../model/form";
import {MyFirstFormComponent} from "../components/form/my-first-form/my-first-form.component";
import {MySecondFormComponent} from "../components/form/my-second-form/my-second-form.component";
import {
  TravelDocFormComponent
} from "../components/form/italy-travel-document-form/travel-doc-form.component";
import {
  FamilyRsdEvidenceFormComponent
} from "../components/form/family-rsd-evidence-form/family-rsd-evidence-form.component";


@Injectable({
  providedIn: 'root'
})
export class CandidateFormService {

  // You need to add an entry to this map for each form that can be displayed in a FormTask.
  // The mapping is from the name of the form to an Angular component.
  private componentMap: Record<string, any> = {
    'MyFirstForm': MyFirstFormComponent,
    'MySecondForm': MySecondFormComponent,
    'FamilyDocForm': FamilyDocFormComponent,
    'TravelDocForm': TravelDocFormComponent,
    'FamilyRsdEvidenceForm': FamilyRsdEvidenceFormComponent,
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

  createOrUpdateMySecondForm(request: MySecondFormData): Observable<MySecondFormData> {
    return this.http.put<MySecondFormData>(`${this.halApiUrl}/my-second-form/MySecondForm`, request);
  }

  getMySecondForm(): Observable<MySecondFormData> {
    return this.http.get<MySecondFormData>(`${this.halApiUrl}/my-second-form/MySecondForm`);
  }

  createOrUpdateTravelDocumentForm(request: TravelDocFormData): Observable<TravelDocFormData> {
    return this.http.post<TravelDocFormData>(`${this.apiUrl}/travel-doc-form`, request);
  }

  getTravelDocumentForm(): Observable<TravelDocFormData> {
    return this.http.get<TravelDocFormData>(`${this.apiUrl}/travel-doc-form`);
  }

  getFormComponentByName(formName: string): any {
    return this.componentMap[formName];
  }
  createOrUpdateFamilyDocsForm(request: FamilyDocFormData) {
    return this.http.put<FamilyDocFormData>(`${this.halApiUrl}/family-doc-form/FamilyDocForm`, request);
  }

  getFamilyDocsForm() {
    return this.http.get<FamilyDocFormData>(`${this.halApiUrl}/family-doc-form/FamilyDocForm`);
  }
  createOrUpdateFamilyRsdEvidenceForm(request: FamilyRsdEvidenceFormData) {
    return this.http.post<FamilyRsdEvidenceFormData>(`${this.apiUrl}/family-rsd-evidence-form`, request);
  }

  getFamilyRsdEvidenceForm() {
    return this.http.get<FamilyRsdEvidenceFormData>(`${this.apiUrl}/family-rsd-evidence-form`);
  }
}
