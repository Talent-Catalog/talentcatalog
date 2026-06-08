import {TestBed} from '@angular/core/testing';

import {CognitoProviderService} from './cognito-provider.service';

describe('CognitoProviderService', () => {
  let service: CognitoProviderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CognitoProviderService]
    });
    service = TestBed.inject(CognitoProviderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
