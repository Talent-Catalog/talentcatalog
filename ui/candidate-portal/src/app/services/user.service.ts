import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from "../../environments/environment";
import {User} from '../model/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  apiUrl = environment.apiUrl + '/user';

  constructor(private http: HttpClient) {}

  getMyUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}`);
  }

  updatePassword(request) {
    return this.http.post(`${this.apiUrl}/password`, request);
  }

  sendResetPassword(request) {
    return this.http.post(`${this.apiUrl}/reset-password-email`, request);
  }

  checkPasswordResetToken(request) {
    return this.http.post(`${this.apiUrl}/check-token`, request);
  }

  resetPassword(request) {
    return this.http.post(`${this.apiUrl}/reset-password`, request);
  }
}
