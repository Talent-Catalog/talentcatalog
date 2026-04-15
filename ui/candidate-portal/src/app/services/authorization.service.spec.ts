import {TestBed} from '@angular/core/testing';

import {AuthorizationService} from './authorization.service';
import {AuthenticationService} from "./authentication.service";

describe('AuthorizationService', () => {

  let service: AuthorizationService;

  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);

    TestBed.configureTestingModule({
      providers: [
        AuthorizationService,
        { provide: AuthenticationService, useValue: spy }
      ]
    });

    service = TestBed.inject(AuthorizationService);
    authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
  });

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthorizationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
