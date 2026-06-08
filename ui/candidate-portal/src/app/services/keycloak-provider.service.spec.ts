import {TestBed} from '@angular/core/testing';

import {KeycloakProviderService} from './keycloak-provider.service';
import {KeycloakService} from "keycloak-angular";

describe('KeycloakProviderService', () => {
  let service: KeycloakProviderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [KeycloakProviderService, KeycloakService]
    });
    service = TestBed.inject(KeycloakProviderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
