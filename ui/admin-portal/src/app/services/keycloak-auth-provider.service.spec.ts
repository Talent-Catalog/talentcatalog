import {TestBed} from '@angular/core/testing';

import {KeycloakProviderService} from './keycloak-provider.service';

describe('KeycloakAuthProviderService', () => {
  let service: KeycloakProviderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KeycloakProviderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
