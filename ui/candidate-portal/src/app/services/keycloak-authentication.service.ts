// keycloak-authentication.service.ts
import {Injectable} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';
import {AuthProvider} from './auth-provider';

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

  constructor(private keycloakService: KeycloakService) {}

  async init(): Promise<boolean> {
    return this.keycloakService.init({
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
  }

  isAuthenticated(): boolean {
    return this.keycloakService.isLoggedIn();
  }

  async login(): Promise<void> {
    await this.keycloakService.login({
      redirectUri: window.location.origin + '/home'
    });
  }

  async register(): Promise<void> {
    await this.keycloakService.register({
      redirectUri: window.location.origin + '/home'
    });
  }

  async logout(): Promise<void> {
    await this.keycloakService.logout(window.location.origin);
  }

  getToken(): string | undefined {
    return this.keycloakService.getKeycloakInstance().token;
  }

  async refreshToken(minValiditySeconds = 30): Promise<void> {
    if (await this.keycloakService.isLoggedIn()) {
      await this.keycloakService.updateToken(minValiditySeconds);
    }
  }
}
