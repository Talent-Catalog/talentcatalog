import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Country} from '../model/country';
import {Observable, of} from 'rxjs';
import {SearchResults} from '../model/search-results';
import {tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CountryService {

  private apiUrl: string = environment.apiUrl + '/country';
  private countries: Country[] = [];
  private countriesRestricted: Country[] = [];
  private tbbDestinations: Country[] = [];

  constructor(private http: HttpClient) { }

  listCountries(): Observable<Country[]> {
    //If we already have the data return it, otherwise get it.
    return this.countries.length > 0 ?
      //"of" turns the data into an Observable
      of(this.countries) :
      this.http.get<Country[]>(`${this.apiUrl}`)
        .pipe(
          //Save data the first time we fetch it
          tap(data => {this.countries = data})
        );
  }

  listCountriesRestricted(): Observable<Country[]> {
    //Get the restricted countries based on the users source countries
    return this.http.get<Country[]>(`${this.apiUrl}/restricted`);
  }

  listTBBDestinations(): Observable<Country[]> {
    //If we already have the data return it, otherwise get it.
    return this.tbbDestinations.length > 0 ?
      //"of" turns the data into an Observable
      of(this.tbbDestinations) :
      this.http.get<Country[]>(`${this.apiUrl}/destinations`)
        .pipe(
          //Save data the first time we fetch it
          tap(data => {this.tbbDestinations = data})
        );
  }

  searchPaged(request): Observable<SearchResults<Country>> {
    return this.http.post<SearchResults<Country>>(`${this.apiUrl}/search-paged`, request);
  }

  get(id: number): Observable<Country> {
    return this.http.get<Country>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Country>  {
    return this.http.post<Country>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Country>  {
    return this.http.put<Country>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
