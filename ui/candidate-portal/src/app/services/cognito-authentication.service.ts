import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {AuthProvider} from './auth-provider';
import {AuthStatus} from './auth-status';
import {AuthProfile} from "./auth-profile";

/**
 * Implementation of AuthProvider for Cognito authentication.
 */
@Injectable()
export class CognitoAuthenticationService implements AuthProvider {
  private readonly status$ = new BehaviorSubject<AuthStatus>({
    initialized: true,
    authenticated: false,
    busy: false,
    error: null
  });

  getStatus(): Observable<AuthStatus> {
    return this.status$.asObservable();
  }

  getCurrentStatus(): AuthStatus {
    return this.status$.value;
  }

  clearError(): void {
    this.patchStatus({ error: null });
  }

  async init(): Promise<boolean> {
    return false;
  }

  isAuthenticated(): boolean {
    return false;
  }

  async login(): Promise<void> {
    this.patchStatus({ error: 'Cognito authentication is not implemented yet.' });
    throw new Error('Cognito authentication is not implemented yet.');
  }

  async register(): Promise<void> {
    this.patchStatus({ error: 'Cognito registration is not implemented yet.' });
    throw new Error('Cognito registration is not implemented yet.');
  }

  async logout(): Promise<void> {
    this.patchStatus({ authenticated: false, error: null });
  }

  getToken(): string | undefined {
    return undefined;
  }

  async getProfile(): Promise<AuthProfile> {
    this.patchStatus({ error: 'Cognito getProfile is not implemented yet.' });
    throw new Error('Cognito getProfile is not implemented yet.');
  }

  async refreshToken(_minValiditySeconds = 30): Promise<void> {
    // no-op
  }

  private patchStatus(patch: Partial<AuthStatus>): void {
    this.status$.next({
      ...this.status$.value,
      ...patch
    });
  }
}
