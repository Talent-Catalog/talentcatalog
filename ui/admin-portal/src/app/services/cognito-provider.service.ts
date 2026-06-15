import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {IdpProvider} from './idp-provider';
import {IdpStatus} from './idp-status';
import {IdpProfile} from "./idp-profile";

/**
 * Implementation of AuthProvider for Cognito authentication.
 */
@Injectable()
export class CognitoProviderService implements IdpProvider {
  private readonly status$ = new BehaviorSubject<IdpStatus>({
    initialized: true,
    authenticated: false,
    busy: false,
    error: null
  });

  getStatus(): Observable<IdpStatus> {
    return this.status$.asObservable();
  }

  getCurrentStatus(): IdpStatus {
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

  async getProfile(): Promise<IdpProfile> {
    this.patchStatus({ error: 'Cognito getProfile is not implemented yet.' });
    throw new Error('Cognito getProfile is not implemented yet.');
  }

  async refreshToken(_minValiditySeconds = 30): Promise<void> {
    // no-op
  }

  private patchStatus(patch: Partial<IdpStatus>): void {
    this.status$.next({
      ...this.status$.value,
      ...patch
    });
  }
}
