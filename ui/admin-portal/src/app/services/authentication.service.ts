import { Injectable } from '@angular/core';
import {LoginRequest} from "../model/base";
import {catchError, map} from "rxjs/operators";
import {JwtResponse} from "../model/jwt-response";
import {Observable, throwError} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService} from "angular-2-local-storage";
import {environment} from "../../environments/environment";
import {User} from "../model/user";
import {Router} from "@angular/router";
import {EncodedQrImage} from "../util/qr";
// import {ChatService} from "./chat.service";

/**
 * Manages authentication - ie login/logout.
 * <p/>
 * See also Auth service which is more about authorization.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  apiUrl = environment.apiUrl + '/auth';

  private loggedInUser: User;

  constructor(
    // private chatService: ChatService,
    private router: Router,
    private http: HttpClient,
    private localStorageService: LocalStorageService
  ) { }


  getLoggedInUser(): User {
    if (!this.loggedInUser) {
      this.loggedInUser = this.localStorageService.get('user');
    }

    if (!AuthenticationService.isValidUserInfo(this.loggedInUser)) {
      console.log("invalid user");
      this.logout();
    }

    return this.loggedInUser;
  }

  getToken(): string {
    return this.localStorageService.get('access-token');
  }

  /**
   * Check that user - possibly retrieved from cache - is not junk
   * @param user User object to check
   */
  private static isValidUserInfo(user: User){

    //Null user is OK
    if (user == null) {
      return true;
    }

    //If user exists it should have a role
    if (user.role) {
      //It should also have a non null readOnly indicator (as an example of another field that
      //should be there and not null
      return user.readOnly != null;
    } else {
      return false;
    }
  }

  login(credentials: LoginRequest) {
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

  logout() {
    this.http.post(`${this.apiUrl}/logout`, null);
    this.localStorageService.remove('user');
    this.localStorageService.remove('access-token');
    this.router.navigate(['login']);
    localStorage.clear();
    // this.chatService.disconnect();
    this.loggedInUser = null;
  }

  mfaSetup(): Observable<EncodedQrImage> {
    return this.http.post<EncodedQrImage>(`${this.apiUrl}/mfa-setup`, null);
  }

  setNewLoggedInUser(new_user) {
    this.localStorageService.set('user', new_user);
  }

  private storeCredentials(response: JwtResponse) {
    this.localStorageService.remove('access-token');
    this.localStorageService.remove('user');
    this.localStorageService.set('access-token', response.accessToken);
    this.localStorageService.set('user', response.user);
    this.loggedInUser = response.user;
  }

}
