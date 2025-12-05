/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { AppComponent } from './app.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthenticationService } from '../services/authentication.service';
import { ChatService } from '../services/chat.service';
import { LanguageService } from '../services/language.service';
import { LanguageLoader } from '../services/language.loader';
import { BrandingService, BrandingInfo } from '../services/branding.service';
import { User } from '../model/user';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let brandingServiceSpy: jasmine.SpyObj<BrandingService>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;
  let languageServiceSpy: jasmine.SpyObj<LanguageService>;
  let languageLoaderSpy: jasmine.SpyObj<LanguageLoader>;
  let routerSpy: jasmine.SpyObj<Router>;
  
  let loggedInUserSubject: BehaviorSubject<User | null>;
  let languageLoadingSubject: BehaviorSubject<boolean>;
  let languageChangedSubject: BehaviorSubject<void>;
  let queryParamMapSubject: BehaviorSubject<any>;

  beforeEach(waitForAsync(() => {
    // Initialize subjects
    loggedInUserSubject = new BehaviorSubject<User | null>(null);
    languageLoadingSubject = new BehaviorSubject<boolean>(false);
    languageChangedSubject = new BehaviorSubject<void>(undefined);
    queryParamMapSubject = new BehaviorSubject<any>({
      get: (key: string) => null
    });

    // Create spies
    const authSpy = jasmine.createSpyObj('AuthenticationService', ['isAuthenticated']);
    authSpy.loggedInUser$ = loggedInUserSubject.asObservable();

    const brandingSpy = jasmine.createSpyObj('BrandingService', ['setPartnerAbbreviation', 'getBrandingInfoFromApi']);
    const chatSpy = jasmine.createSpyObj('ChatService', ['cleanUp']);
    const languageSpy = jasmine.createSpyObj('LanguageService', ['getSelectedLanguage', 'isSelectedLanguageRtl']);
    const languageLoadSpy = jasmine.createSpyObj('LanguageLoader', ['load']);
    languageLoadSpy.languageLoading$ = languageLoadingSubject.asObservable();
    languageSpy.languageChanged$ = languageChangedSubject.asObservable();
    
    const routSpy = jasmine.createSpyObj('Router', ['navigate']);
    routSpy.events = new BehaviorSubject<any>(null);

    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      declarations: [AppComponent],
      providers: [
        { provide: AuthenticationService, useValue: authSpy },
        { provide: BrandingService, useValue: brandingSpy },
        { provide: ChatService, useValue: chatSpy },
        { provide: LanguageService, useValue: languageSpy },
        { provide: LanguageLoader, useValue: languageLoadSpy },
        { provide: Router, useValue: routSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: queryParamMapSubject.asObservable(),
            snapshot: {
              queryParams: {}
            }
          }
        },
        TranslateService
      ]
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    brandingServiceSpy = TestBed.inject(BrandingService) as jasmine.SpyObj<BrandingService>;
    chatServiceSpy = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
    languageServiceSpy = TestBed.inject(LanguageService) as jasmine.SpyObj<LanguageService>;
    languageLoaderSpy = TestBed.inject(LanguageLoader) as jasmine.SpyObj<LanguageLoader>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  }));

  beforeEach(() => {
    // Set default return values
    languageServiceSpy.getSelectedLanguage.and.returnValue('en');
    languageServiceSpy.isSelectedLanguageRtl.and.returnValue(false);
    brandingServiceSpy.getBrandingInfoFromApi.and.returnValue(of({
      logo: 'logo.png',
      partnerName: 'Test Partner',
      websiteUrl: 'https://test.com'
    }));

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  describe('Branding Tests', () => {
    it('should set isTBBPartner to true when partner is Talent Beyond Boundaries', fakeAsync(() => {
      const tbbBrandingInfo: BrandingInfo = {
        logo: 'tbb-logo.png',
        partnerName: 'Talent Beyond Boundaries',
        websiteUrl: 'https://talentbeyondboundaries.org'
      };

      brandingServiceSpy.getBrandingInfoFromApi.and.returnValue(of(tbbBrandingInfo));

      fixture.detectChanges();
      tick();

      expect(component.isTBBPartner).toBe(true);
    }));

    it('should set isTBBPartner to false for other partners', fakeAsync(() => {
      const otherBrandingInfo: BrandingInfo = {
        logo: 'other-logo.png',
        partnerName: 'Other Partner',
        websiteUrl: 'https://other.com'
      };

      brandingServiceSpy.getBrandingInfoFromApi.and.returnValue(of(otherBrandingInfo));

      fixture.detectChanges();
      tick();

      expect(component.isTBBPartner).toBe(false);
    }));

    it('should set isTBBPartner to false on branding API error', fakeAsync(() => {
      brandingServiceSpy.getBrandingInfoFromApi.and.returnValue(
        throwError(() => new Error('API Error'))
      );

      fixture.detectChanges();
      tick();

      expect(component.isTBBPartner).toBe(false);
    }));

    it('should check branding when user login state changes', fakeAsync(() => {
      fixture.detectChanges();
      const initialCallCount = brandingServiceSpy.getBrandingInfoFromApi.calls.count();

      const mockUser: User = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com'
      } as User;

      loggedInUserSubject.next(mockUser);
      tick();

      expect(brandingServiceSpy.getBrandingInfoFromApi.calls.count()).toBeGreaterThan(initialCallCount);
    }));
  });

  describe('Query Parameter Handling', () => {
    it('should set partner abbreviation from query param', fakeAsync(() => {
      const mockQueryParamMap = {
        get: (key: string) => key === 'p' ? 'TBB' : null
      };

      queryParamMapSubject.next(mockQueryParamMap);
      fixture.detectChanges();
      tick();

      expect(brandingServiceSpy.setPartnerAbbreviation).toHaveBeenCalledWith('TBB');
    }));

    it('should check branding when query params change', fakeAsync(() => {
      fixture.detectChanges();
      const initialCallCount = brandingServiceSpy.getBrandingInfoFromApi.calls.count();

      const mockQueryParamMap = {
        get: (key: string) => key === 'p' ? 'PARTNER' : null
      };

      queryParamMapSubject.next(mockQueryParamMap);
      tick();

      expect(brandingServiceSpy.getBrandingInfoFromApi.calls.count()).toBeGreaterThan(initialCallCount);
    }));

    it('should handle initial query params from snapshot', fakeAsync(() => {
      const activatedRoute = TestBed.inject(ActivatedRoute);
      activatedRoute.snapshot.queryParams = { p: 'INITIAL' };

      // Create new component instance with snapshot params
      const newFixture = TestBed.createComponent(AppComponent);
      newFixture.detectChanges();
      tick();

      expect(brandingServiceSpy.setPartnerAbbreviation).toHaveBeenCalledWith('INITIAL');
    }));
  });

  describe('User Logout', () => {
    it('should clean up chat service and navigate to login on logout', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      loggedInUserSubject.next(null);
      tick();

      expect(chatServiceSpy.cleanUp).toHaveBeenCalled();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['login']);
    }));
  });

  describe('Language Management', () => {
    it('should update RTL setting when language changes', fakeAsync(() => {
      languageServiceSpy.isSelectedLanguageRtl.and.returnValue(true);
      
      fixture.detectChanges();
      tick();

      languageChangedSubject.next();
      tick();

      expect(component.rtl).toBe(true);
    }));

    it('should update loading state when language is loading', fakeAsync(() => {
      fixture.detectChanges();
      
      languageLoadingSubject.next(true);
      tick();

      expect(component.loading).toBe(true);

      languageLoadingSubject.next(false);
      tick();

      expect(component.loading).toBe(false);
    }));
  });
});
