import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Occupation} from '../model/occupation';
import {map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class OccupationService {

  private apiUrl: string = environment.apiUrl + '/occupation';

  constructor(private http: HttpClient) { }

  listOccupations(): Observable<Occupation[]> {
    return this.http.get<Occupation[]>(`${this.apiUrl}`).pipe(
      map((items: Occupation[], index: number) => {
        const unknown: Occupation = items.find(x => x.id === 0);
        const i: number = items.indexOf(unknown);
        if (unknown){
          items.splice(i, 1);
          items.push(unknown);
        }
        return items;
      }
    ))
  }

}
