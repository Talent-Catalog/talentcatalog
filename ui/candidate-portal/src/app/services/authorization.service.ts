import {Injectable} from '@angular/core';
import {AuthenticationService} from "./authentication.service";

@Injectable({
  providedIn: 'root'
})
export class AuthorizationService {

  constructor(private authenticationService: AuthenticationService) {
  }

  canViewChats(): boolean {
    //todo This can be computed from policy rules.
    return false;
  }

}
