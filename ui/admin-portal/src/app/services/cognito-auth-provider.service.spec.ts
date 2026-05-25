import {TestBed} from '@angular/core/testing';

import {CognitoProviderService} from './cognito-provider.service';

describe('CognitoAuthProviderService', () => {
  let service: CognitoProviderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CognitoProviderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
