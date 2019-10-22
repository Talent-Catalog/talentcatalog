import {Injectable} from '@angular/core';
import {JwtResponse} from "../model/jwt-response";
import {BehaviorSubject, Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {environment} from "../../environments/environment";
import {Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService} from "angular-2-local-storage";
import {User} from "../model/user";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  apiUrl = environment.apiUrl + '/auth';

  private user: User;
  private _user: BehaviorSubject<User> = new BehaviorSubject(this.getLoggedInUser());
  public readonly user$: Observable<User> = this._user.asObservable();

  constructor(private router: Router,
              private http: HttpClient,
              private localStorageService: LocalStorageService) {
  }

  login(credentials) {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      map((response: JwtResponse) => {
        this.storeCredentials(response);
      }),
      catchError(e => {
          console.log('error', e);
          return throwError(e);
        }
      )
    );
  }

  isAuthenticated(): boolean {
    return this.getLoggedInUser() != null;
  }

  getLoggedInUser(): User {
    if (!this.user) {
      // could be a page reload, check localstorage
      const user = this.localStorageService.get('user');
      this.user = <User>user;
    }
    return this.user;
  }

  getToken(): string {
    return this.localStorageService.get('access-token');
  }

  logout() {
    return this.http.post(`${this.apiUrl}/logout`, null).pipe(
      map(() => this.clearCredentials()),
      catchError(e => throwError(e))
    );
  }

  register(request: any) {
    return this.http.post<JwtResponse>(`${this.apiUrl}/register`, request).pipe(
      map((response) => this.storeCredentials(response)),
      catchError((e) => {return throwError(e)})
    );
  }

  private storeCredentials(response: JwtResponse) {
    this.clearCredentials();
    this.localStorageService.set('access-token', response.accessToken);
    this.localStorageService.set('user', response.user);
    this._user.next(response.user);
  }

  private clearCredentials() {
    this.localStorageService.remove('access-token');
    this.localStorageService.remove('user');
    this._user.next(null);
    /* DEBUG */
    console.log('this.user', this._user);
  }

}
