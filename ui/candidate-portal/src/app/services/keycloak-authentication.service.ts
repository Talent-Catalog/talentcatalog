// keycloak-authentication.service.ts
import {Injectable} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';
import {AuthProvider} from './auth-provider';

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
