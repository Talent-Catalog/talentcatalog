import { TestBed } from '@angular/core/testing';

import { CognitoAuthenticationService } from './cognito-authentication.service';

describe('CognitoAuthenticationService', () => {
  let service: CognitoAuthenticationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CognitoAuthenticationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
