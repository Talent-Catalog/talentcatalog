import {TestBed} from '@angular/core/testing';

import {KeycloakAuthProviderService} from './keycloak-auth-provider.service';

describe('KeycloakAuthProviderService', () => {
  let service: KeycloakAuthProviderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KeycloakAuthProviderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
