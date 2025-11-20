import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {FamilyDocFormComponent} from "../components/form/family-doc-form/family-doc-form.component";
import {
  DependantsInfoFormData,
  MyFirstFormData,
  RefugeeStatusInfoFormData,
  TravelInfoFormData
} from "../model/form";
import {MyFirstFormComponent} from "../components/form/my-first-form/my-first-form.component";
import {
  TravelInfoFormComponent
} from "../components/form/italy-travel-document-form/travel-info-form.component";
import {
  DependantsRefugeeStatusInfoFormComponent
} from "../components/form/family-rsd-evidence-form/dependants-refugee-status-info-form.component";
import {
  RefugeeStatusInfoFormComponent
} from "../components/form/rsd-evidence-form/refugee-status-info-form.component";


@Injectable({
  providedIn: 'root'
})
export class CandidateFormService {

  // You need to add an entry to this map for each form that can be displayed in a FormTask.
  // The mapping is from the name of the form to an Angular component.
  private componentMap: Record<string, any> = {
    'MyFirstForm': MyFirstFormComponent,
    'FamilyDocForm': FamilyDocFormComponent,
    'TravelInfoForm': TravelInfoFormComponent,
    'DependantsRefugeeStatusInfoForm': DependantsRefugeeStatusInfoFormComponent,
    'RefugeeStatusInfoForm': RefugeeStatusInfoFormComponent
  }

  apiUrl: string = environment.apiUrl + '/form';

  constructor(private http: HttpClient) { }

  createOrUpdateMyFirstForm(request: MyFirstFormData): Observable<MyFirstFormData> {
    return this.http.post<MyFirstFormData>(`${this.apiUrl}/my-first-form`, request);
  }

  getMyFirstForm(): Observable<MyFirstFormData> {
    return this.http.get<MyFirstFormData>(`${this.apiUrl}/my-first-form`);
  }

  createOrUpdateTravelInfoForm(request: TravelInfoFormData): Observable<TravelInfoFormData> {
    return this.http.post<TravelInfoFormData>(`${this.apiUrl}/travel-info-form`, request);
  }

  getTravelInfoForm(): Observable<TravelInfoFormData> {
    return this.http.get<TravelInfoFormData>(`${this.apiUrl}/travel-info-form`);
  }

  getFormComponentByName(formName: string): any {
    return this.componentMap[formName];
  }
  createOrUpdateDependantsInfoForm(request: DependantsInfoFormData) {
    return this.http.post<DependantsInfoFormData>(`${this.apiUrl}/dependants-info-form`, request);
  }

  getDependantsInfoForm() {
    return this.http.get<DependantsInfoFormData>(`${this.apiUrl}/dependants-info-form`);
  }

  createOrUpdateRefugeeStatusInfoForm(request: RefugeeStatusInfoFormData): Observable<RefugeeStatusInfoFormData> {
    return this.http.post<RefugeeStatusInfoFormData>(`${this.apiUrl}/refugee-status-info-form`, request);
  }

  getRefugeeStatusInfoForm(): Observable<RefugeeStatusInfoFormData> {
    return this.http.get<RefugeeStatusInfoFormData>(`${this.apiUrl}/refugee-status-info-form`);
  }
}
