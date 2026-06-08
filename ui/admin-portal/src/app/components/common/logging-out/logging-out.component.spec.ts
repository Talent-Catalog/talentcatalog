import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {ActivatedRoute, convertToParamMap, Router} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';

import {LoggingOutComponent} from './logging-out.component';
import {AuthenticationService} from '../../../services/authentication.service';

describe('LoggingOutComponent', () => {
  let fixture: ComponentFixture<LoggingOutComponent>;
  let component: LoggingOutComponent;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authenticationService = jasmine.createSpyObj<AuthenticationService>(
      'AuthenticationService',
      ['logout']
    );

    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        LoggingOutComponent,
        TranslateModule.forRoot()
      ],
      providers: [
        {
          provide: AuthenticationService,
          useValue: authenticationService
        },
        {
          provide: Router,
          useValue: router
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap({
                reason: 'Session expired',
                returnUrl: '/dashboard'
              })
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoggingOutComponent);
    component = fixture.componentInstance;
  });

  it('should read reason and returnUrl from query params on init', fakeAsync(() => {
    fixture.detectChanges();

    expect(component.reason).toBe('Session expired');
    expect(component.returnUrl).toBe('/dashboard');
    expect(component.secondsRemaining).toBe(180);

    component.ngOnDestroy();
  }));

  it('should count down every second', fakeAsync(() => {
    fixture.detectChanges();

    expect(component.secondsRemaining).toBe(180);

    tick(1000);
    expect(component.secondsRemaining).toBe(179);

    tick(1000);
    expect(component.secondsRemaining).toBe(178);

    component.ngOnDestroy();
  }));

  it('should logout and navigate to login when logout is called', () => {
    component.returnUrl = '/dashboard';

    component.logout();

    expect(authenticationService.logout).toHaveBeenCalled();

    expect(router.navigate).toHaveBeenCalledWith(
      ['/login'],
      {
        queryParams: {
          returnUrl: '/dashboard'
        }
      }
    );
  });

  it('should logout but not navigate when returnUrl starts with /login', () => {
    component.returnUrl = '/login';

    component.logout();

    expect(authenticationService.logout).toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should automatically logout when countdown reaches zero', fakeAsync(() => {
    spyOn(component, 'logout').and.callThrough();

    fixture.detectChanges();

    tick(180_000);

    expect(component.logout).toHaveBeenCalled();
    expect(authenticationService.logout).toHaveBeenCalled();

    expect(router.navigate).toHaveBeenCalledWith(
      ['/login'],
      {
        queryParams: {
          returnUrl: '/dashboard'
        }
      }
    );
  }));

  it('should stop countdown when destroyed', fakeAsync(() => {
    fixture.detectChanges();

    expect(component.secondsRemaining).toBe(180);

    component.ngOnDestroy();

    tick(5_000);

    expect(component.secondsRemaining).toBe(180);
    expect(authenticationService.logout).not.toHaveBeenCalled();
  }));
});
