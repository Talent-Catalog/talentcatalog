import {Injectable} from '@angular/core';
import {JwtResponse} from "../model/jwt-response";
import {throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {environment} from "../../environments/environment";
import {Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService} from "angular-2-local-storage";
import {Candidate} from "../model/candidate";
import {User} from "../model/user";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  apiUrl = environment.apiUrl + '/auth';

  private user: Candidate;
  private loggedInUser: User;

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

  // getLoggedInCandidate(): Candidate {
  //   if (!this.user) {
  //     // could be a page reload, check localstorage
  //     const user = this.localStorageService.get('user');
  //     this.user = <Candidate>user;
  //   }
  //   return this.user;
  // }

  getLoggedInUser(): User {
    if(!this.loggedInUser){
      const user = this.localStorageService.get('user');
      this.loggedInUser = <User>user;
    }
    return this.loggedInUser;
  }

  setNewLoggedInUser(new_user) {
    this.localStorageService.set('user', new_user);
  }

  getToken(): string {
    return this.localStorageService.get('access-token');
  }

  logout() {
    this.http.post(`${this.apiUrl}/logout`, null);
    this.localStorageService.remove('user');
    this.localStorageService.remove('access-token');
  }

  private storeCredentials(response: JwtResponse) {
    this.localStorageService.remove('access-token');
    this.localStorageService.remove('user');
    this.localStorageService.set('access-token', response.accessToken);
    this.localStorageService.set('user', response.user);
    this.loggedInUser = response.user;
  }

}
