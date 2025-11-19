import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {FamilyDocFormComponent} from "../components/form/family-doc-form/family-doc-form.component";
import {
  DependantsInfoFormData,
  MyFirstFormData,
  RsdEvidenceFormData,
  TravelDocFormData
} from "../model/form";
import {MyFirstFormComponent} from "../components/form/my-first-form/my-first-form.component";
import {
  TravelDocFormComponent
} from "../components/form/italy-travel-document-form/travel-doc-form.component";
import {
  FamilyRsdEvidenceFormComponent
} from "../components/form/family-rsd-evidence-form/family-rsd-evidence-form.component";
import {
  RsdEvidenceFormComponent
} from "../components/form/rsd-evidence-form/rsd-evidence-form.component";


@Injectable({
  providedIn: 'root'
})
export class CandidateFormService {

  // You need to add an entry to this map for each form that can be displayed in a FormTask.
  // The mapping is from the name of the form to an Angular component.
  private componentMap: Record<string, any> = {
    'MyFirstForm': MyFirstFormComponent,
    'FamilyDocForm': FamilyDocFormComponent,
    'TravelDocForm': TravelDocFormComponent,
    'FamilyRsdEvidenceForm': FamilyRsdEvidenceFormComponent,
    'RsdEvidenceForm': RsdEvidenceFormComponent
  }

  apiUrl: string = environment.apiUrl + '/form';

  constructor(private http: HttpClient) { }

  createOrUpdateMyFirstForm(request: MyFirstFormData): Observable<MyFirstFormData> {
    return this.http.post<MyFirstFormData>(`${this.apiUrl}/my-first-form`, request);
  }

  getMyFirstForm(): Observable<MyFirstFormData> {
    return this.http.get<MyFirstFormData>(`${this.apiUrl}/my-first-form`);
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
  createOrUpdateFamilyDocsForm(request: DependantsInfoFormData) {
    return this.http.post<DependantsInfoFormData>(`${this.apiUrl}/family-doc-form`, request);
  }

  getDependantsInfoForm() {
    return this.http.get<DependantsInfoFormData>(`${this.apiUrl}/dependants-info-form`);
  }

  createOrUpdateRsdEvidenceForm(request: RsdEvidenceFormData): Observable<RsdEvidenceFormData> {
    return this.http.post<RsdEvidenceFormData>(`${this.apiUrl}/rsd-evidence-form`, request);
  }

  getRsdEvidenceForm(): Observable<RsdEvidenceFormData> {
    return this.http.get<RsdEvidenceFormData>(`${this.apiUrl}/rsd-evidence-form`);
  }
}
