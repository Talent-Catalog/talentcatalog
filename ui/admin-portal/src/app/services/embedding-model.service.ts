import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {EmbeddingModel} from '../model/embedding-model';

@Injectable({
  providedIn: 'root'
})
export class EmbeddingModelService {
  private apiUrl: string = environment.apiUrl + '/embedding-model';

  constructor(private http: HttpClient) { }

  loadReadyModels(): Observable<EmbeddingModel[]> {
    //Get the embedding models that are ready for use.
    return this.http.get<EmbeddingModel[]>(`${this.apiUrl}/ready`);
  }

}
