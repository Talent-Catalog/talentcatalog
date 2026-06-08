import {TestBed} from '@angular/core/testing';
import {KeycloakService} from 'keycloak-angular';

import {KeycloakProviderService} from './keycloak-provider.service';

describe('KeycloakProviderService', () => {
  let service: KeycloakProviderService;
  let keycloakService: jasmine.SpyObj<KeycloakService>;

  beforeEach(() => {
    keycloakService = jasmine.createSpyObj<KeycloakService>('KeycloakService', [
      'init',
      'isLoggedIn',
      'login',
      'register',
      'logout',
      'loadUserProfile',
      'getKeycloakInstance',
      'updateToken'
    ]);

    TestBed.configureTestingModule({
      providers: [
        KeycloakProviderService,
        { provide: KeycloakService, useValue: keycloakService }
      ]
    });

    service = TestBed.inject(KeycloakProviderService);
  });

  it('should start with an uninitialized unauthenticated status', () => {
    expect(service.getCurrentStatus()).toEqual({
      initialized: false,
      authenticated: false,
      busy: false,
      error: null
    });
  });

  it('should initialize Keycloak successfully', async () => {
    keycloakService.init.and.resolveTo(true);

    const authenticated = await service.init();

    expect(authenticated).toBeTrue();
    expect(keycloakService.init).toHaveBeenCalled();

    expect(service.getCurrentStatus()).toEqual({
      initialized: true,
      authenticated: true,
      busy: false,
      error: null
    });
  });

  it('should update status when init fails', async () => {
    keycloakService.init.and.rejectWith(new Error('Init failed'));

    const authenticated = await service.init();

    expect(authenticated).toBeFalse();

    expect(service.getCurrentStatus().initialized).toBeTrue();
    expect(service.getCurrentStatus().authenticated).toBeFalse();
    expect(service.getCurrentStatus().busy).toBeFalse();
    expect(service.getCurrentStatus().error).toContain(
      'Failed to initialize authentication service'
    );
  });

  it('should delegate isAuthenticated to KeycloakService', () => {
    keycloakService.isLoggedIn.and.returnValue(true);

    expect(service.isAuthenticated()).toBeTrue();
    expect(keycloakService.isLoggedIn).toHaveBeenCalledOnceWith();
  });

  it('should login with redirect uri and locale', async () => {
    keycloakService.login.and.resolveTo();

    await service.login('/login-callback', 'fr');

    expect(keycloakService.login).toHaveBeenCalledOnceWith({
      redirectUri: window.location.origin + '/login-callback',
      locale: 'fr'
    });

    expect(service.getCurrentStatus().busy).toBeFalse();
  });

  it('should update status and rethrow when login fails', async () => {
    const error = new Error('Login failed');
    keycloakService.login.and.rejectWith(error);

    await expectAsync(service.login('/login-callback'))
    .toBeRejectedWith(error);

    expect(service.getCurrentStatus().busy).toBeFalse();
    expect(service.getCurrentStatus().error).toContain('Login failed');
  });

  it('should register with redirect uri and locale', async () => {
    keycloakService.register.and.resolveTo();

    await service.register('/register-callback', 'ar');

    expect(keycloakService.register).toHaveBeenCalledOnceWith({
      redirectUri: window.location.origin + '/register-callback',
      locale: 'ar'
    });

    expect(service.getCurrentStatus().busy).toBeFalse();
  });

  it('should logout and mark user unauthenticated', async () => {
    keycloakService.logout.and.resolveTo();

    await service.logout();

    expect(keycloakService.logout).toHaveBeenCalledOnceWith();

    expect(service.getCurrentStatus().authenticated).toBeFalse();
    expect(service.getCurrentStatus().busy).toBeFalse();
    expect(service.getCurrentStatus().error).toBeNull();
  });

  it('should build an IdpProfile from Keycloak profile and token data', async () => {
    keycloakService.loadUserProfile.and.resolveTo({
      email: 'candidate@example.org',
      firstName: 'Test',
      lastName: 'Candidate'
    } as any);

    keycloakService.getKeycloakInstance.and.returnValue({
      subject: 'keycloak-user-id-123',
      tokenParsed: {
        iss: 'http://localhost:8082/realms/talentcatalog'
      }
    } as any);

    const profile = await service.getProfile();

    expect(profile).toEqual({
      idpIssuer: 'http://localhost:8082/realms/talentcatalog',
      idpSubject: 'keycloak-user-id-123',
      email: 'candidate@example.org',
      firstName: 'Test',
      lastName: 'Candidate'
    });

    expect(keycloakService.loadUserProfile).toHaveBeenCalledOnceWith(true);
  });

  it('should return token from Keycloak instance', () => {
    keycloakService.getKeycloakInstance.and.returnValue({
      token: 'access-token'
    } as any);

    expect(service.getToken()).toBe('access-token');
  });

  it('should return undefined and set error when token cannot be read', () => {
    keycloakService.getKeycloakInstance.and.throwError('No Keycloak instance');

    expect(service.getToken()).toBeUndefined();
    expect(service.getCurrentStatus().error).toContain(
      'Could not read authentication token'
    );
  });

  it('should refresh token when logged in', async () => {
    keycloakService.isLoggedIn.and.returnValue(true);
    keycloakService.updateToken.and.resolveTo(true);

    await service.refreshToken(60);

    expect(keycloakService.isLoggedIn).toHaveBeenCalledOnceWith();
    expect(keycloakService.updateToken).toHaveBeenCalledOnceWith(60);
  });

  it('should not refresh token when not logged in', async () => {
    keycloakService.isLoggedIn.and.returnValue(false);

    await service.refreshToken();

    expect(keycloakService.updateToken).not.toHaveBeenCalled();
  });

  it('should update status and rethrow when refresh fails', async () => {
    const error = new Error('Refresh failed');

    keycloakService.isLoggedIn.and.returnValue(true);
    keycloakService.updateToken.and.rejectWith(error);

    await expectAsync(service.refreshToken()).toBeRejectedWith(error);

    expect(service.getCurrentStatus().authenticated).toBeFalse();
    expect(service.getCurrentStatus().error).toContain(
      'Authentication session refresh failed'
    );
  });

  it('should clear error', () => {
    keycloakService.getKeycloakInstance.and.throwError('No token');

    service.getToken();
    expect(service.getCurrentStatus().error).not.toBeNull();

    service.clearError();

    expect(service.getCurrentStatus().error).toBeNull();
  });
});
