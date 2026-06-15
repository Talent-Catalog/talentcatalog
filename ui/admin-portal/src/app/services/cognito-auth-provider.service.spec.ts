import {TestBed} from '@angular/core/testing';

import {CognitoAuthProviderService} from './cognito-auth-provider.service';

describe('CognitoAuthProviderService', () => {
  let service: CognitoAuthProviderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CognitoAuthProviderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
