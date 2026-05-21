import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {KeycloakService} from 'keycloak-angular';
import {AuthProvider} from './auth-provider';
import {AuthStatus} from './auth-status';
import {reportAuthError} from './auth-error.util';
import {AuthProfile} from "./auth-profile";

/**
 * Keycloak authentication service.
 * See https://www.npmjs.com/package/keycloak-angular?activeTab=readme for installing Keycloak
 * plus the Keycloak angular library. Note also the table of version compatability with
 * Angular versions.
 * I installed with
 * <pre>
 * npm install keycloak-angular@15 keycloak-js
 * </pre>
 * which is compatible with Angular 17.
 */
@Injectable()
export class KeycloakAuthenticationService implements AuthProvider {

  private readonly status$ = new BehaviorSubject<AuthStatus>({
    initialized: false,
    authenticated: false,
    busy: false,
    error: null
  });

  constructor(private keycloakService: KeycloakService) {}

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
    this.patchStatus({ busy: true, error: null });

    try {
      const authenticated = await this.keycloakService.init({
        config: {
          url: 'http://localhost:8082',
          realm: 'talentcatalog',
          clientId: 'grn-candidate'
        },
        initOptions: {
          onLoad: 'check-sso',
          pkceMethod: 'S256'
        },
        enableBearerInterceptor: false
      });

      this.patchStatus({
        initialized: true,
        authenticated,
        busy: false,
        error: null
      });

      return authenticated;
    } catch (e) {
      const message = reportAuthError('Failed to initialize authentication service', e);

      this.patchStatus({
        initialized: true,
        authenticated: false,
        busy: false,
        error: message
      });

      return false;
    }
  }

  isAuthenticated(): boolean {
    return this.keycloakService.isLoggedIn();
  }

  async login(): Promise<void> {
    this.patchStatus({ busy: true, error: null });

    try {
      await this.keycloakService.login({
        redirectUri: window.location.origin + '/?authAction=login'
      });

      this.patchStatus({ busy: false });
    } catch (e) {
      const message = reportAuthError('Login failed', e);

      this.patchStatus({
        busy: false,
        error: message
      });

      throw e;
    }
  }

  async register(): Promise<void> {
    this.patchStatus({ busy: true, error: null });

    try {
      await this.keycloakService.register({
        redirectUri: window.location.origin + '/?authAction=register'
      });

      this.patchStatus({ busy: false });
    } catch (e) {
      const message = reportAuthError('Registration failed', e);

      this.patchStatus({
        busy: false,
        error: message
      });

      throw e;
    }
  }

  async logout(): Promise<void> {
    this.patchStatus({ busy: true, error: null });

    try {
      await this.keycloakService.logout(window.location.origin);

      this.patchStatus({
        busy: false,
        authenticated: false,
        error: null
      });
    } catch (e) {
      const message = reportAuthError('Logout failed', e);

      this.patchStatus({
        busy: false,
        error: message
      });

      throw e;
    }
  }

  async getProfile(): Promise<AuthProfile> {
    const profile = await this.keycloakService.loadUserProfile(true);
    const keycloak = this.keycloakService.getKeycloakInstance();

    return {
      idpIssuer: keycloak.tokenParsed?.iss as string,
      idpSubject: keycloak.subject,
      email: profile.email,
      firstName: profile.firstName,
      lastName: profile.lastName
    };
  }

  getToken(): string | undefined {
    try {
      return this.keycloakService.getKeycloakInstance().token;
    } catch (e) {
      const message = reportAuthError('Could not read authentication token', e);
      this.patchStatus({ error: message });
      return undefined;
    }
  }

  async refreshToken(minValiditySeconds = 30): Promise<void> {
    try {
      const loggedIn = await this.keycloakService.isLoggedIn();
      if (loggedIn) {
        await this.keycloakService.updateToken(minValiditySeconds);
      }
    } catch (e) {
      const message = reportAuthError('Authentication session refresh failed', e);

      this.patchStatus({
        authenticated: false,
        error: message
      });

      throw e;
    }
  }

  private patchStatus(patch: Partial<AuthStatus>): void {
    this.status$.next({
      ...this.status$.value,
      ...patch
    });
  }
}
