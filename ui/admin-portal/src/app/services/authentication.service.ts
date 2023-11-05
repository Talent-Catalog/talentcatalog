import {Injectable, OnDestroy} from '@angular/core';
import {LoginRequest} from "../model/base";
import {catchError, map} from "rxjs/operators";
import {JwtResponse} from "../model/jwt-response";
import {Observable, throwError, Subject} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService} from "angular-2-local-storage";
import {environment} from "../../environments/environment";
import {User} from "../model/user";
import {Router} from "@angular/router";
import {EncodedQrImage} from "../util/qr";

/**
 * Manages authentication - ie login/logout.
 * <p/>
 * See also Auth service which is more about authorization.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthenticationService implements OnDestroy {
  apiUrl = environment.apiUrl + '/auth';

  /**
   * Stores current logged in state
   * @private
   */
  private loggedInUser: User = null;

  /**
   * Can be used to subscribe to logged in state changes - ie logins and logouts
   */
  loggedInUser$ = new Subject<User>();

  constructor(
    private router: Router,
    private http: HttpClient,
    private localStorageService: LocalStorageService
  ) {}

  ngOnDestroy(): void {
    //This will close any subscriptions - freeing resources.
    //See https://stackoverflow.com/a/77426261/929968
    this.loggedInUser$.complete();
  }

  getLoggedInUser(): User {
    if (!this.loggedInUser) {
      //We don't have a loggedInUser stored - can we pick it up from storage?
      const user: User = this.localStorageService.get('user');
      if (!AuthenticationService.isValidUserInfo(user)) {
        console.log("invalid user");
        this.logout();
      } else {
        //Update logged-in user retrieved from storage
        this.setLoggedInUser(user);
      }
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
    this.router.navigate(['login']);
    this.localStorageService.remove('user');
    this.localStorageService.remove('access-token');
    localStorage.clear();

    this.setLoggedInUser(null)
  }

  mfaSetup(): Observable<EncodedQrImage> {
    return this.http.post<EncodedQrImage>(`${this.apiUrl}/mfa-setup`, null);
  }

  setLoggedInUser(loggedInUser: User) {
    this.loggedInUser = loggedInUser;
    this.localStorageService.set('user', this.loggedInUser);
    this.loggedInUser$.next(this.loggedInUser);
  }

  private storeCredentials(response: JwtResponse) {
    //Remove any old credentials from storage
    this.localStorageService.remove('access-token');
    this.localStorageService.remove('user');

    //Update new credentials in storage
    this.localStorageService.set('access-token', response.accessToken);

    this.setLoggedInUser(response.user);
  }

}
