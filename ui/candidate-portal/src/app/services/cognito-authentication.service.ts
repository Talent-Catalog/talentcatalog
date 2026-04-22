// cognito-authentication.service.ts
import {Injectable} from '@angular/core';
import {AuthProvider} from './auth-provider';

@Injectable()
export class CognitoAuthenticationService implements AuthProvider {
  async init(): Promise<boolean> {
    return false;
  }

  isAuthenticated(): boolean {
    return false;
  }

  async login(): Promise<void> {
    throw new Error('CognitoAuthenticationService not implemented yet.');
  }

  async register(): Promise<void> {
    throw new Error('CognitoAuthenticationService not implemented yet.');
  }

  async logout(): Promise<void> {
    localStorage.clear();
  }

  getToken(): string | undefined {
    return undefined;
  }

  async refreshToken(_minValiditySeconds = 30): Promise<void> {
    // no-op
  }
}
