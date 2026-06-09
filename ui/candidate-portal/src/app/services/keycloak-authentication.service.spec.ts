import { TestBed } from '@angular/core/testing';

import { KeycloakAuthenticationService } from './keycloak-authentication.service';

describe('KeycloakAuthenticationService', () => {
  let service: KeycloakAuthenticationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KeycloakAuthenticationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
