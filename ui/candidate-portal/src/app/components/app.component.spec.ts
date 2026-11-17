/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import type {MockedObject} from "vitest";
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import {ActivatedRoute, Router} from '@angular/router';
import {BehaviorSubject} from 'rxjs';
import {AppComponent} from './app.component';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {AuthenticationService} from '../services/authentication.service';
import {ChatService} from '../services/chat.service';
import {LanguageService} from '../services/language.service';
import {LanguageLoader} from '../services/language.loader';
import {BrandingService} from '../services/branding.service';
import {User} from '../model/user';
import {TcInstanceType} from '../model/tc-instance-type';
import {environment} from '../../environments/environment';

describe('AppComponent', () => {
    let component: AppComponent;
    let fixture: ComponentFixture<AppComponent>;
    let authServiceSpy: MockedObject<AuthenticationService>;
    let brandingServiceSpy: MockedObject<BrandingService>;
    let chatServiceSpy: MockedObject<ChatService>;
    let languageServiceSpy: MockedObject<LanguageService>;
    let languageLoaderSpy: MockedObject<LanguageLoader>;
    let routerSpy: MockedObject<Router>;

    let loggedInUserSubject: BehaviorSubject<User | null>;
    let languageLoadingSubject: BehaviorSubject<boolean>;
    let languageChangedSubject: BehaviorSubject<void>;
    let queryParamMapSubject: BehaviorSubject<any>;
    let originalEnvironmentName: string;

    beforeEach(waitForAsync(() => {
        // Initialize subjects
        loggedInUserSubject = new BehaviorSubject<User | null>(null);
        languageLoadingSubject = new BehaviorSubject<boolean>(false);
        languageChangedSubject = new BehaviorSubject<void>(undefined);
        queryParamMapSubject = new BehaviorSubject<any>({
            get: (key: string) => null
        });

        // Create spies
        const authSpy = {
            isAuthenticated: vi.fn().mockName("AuthenticationService.isAuthenticated"),
            getTcInstanceType: vi.fn().mockName("AuthenticationService.getTcInstanceType")
        };
        Object.assign(authSpy, { loggedInUser$: loggedInUserSubject.asObservable() });

        const brandingSpy = {
            setPartnerAbbreviation: vi.fn().mockName("BrandingService.setPartnerAbbreviation")
        };
        const chatSpy = {
            cleanUp: vi.fn().mockName("ChatService.cleanUp")
        };
        const languageSpy = {
            getSelectedLanguage: vi.fn().mockName("LanguageService.getSelectedLanguage"),
            isSelectedLanguageRtl: vi.fn().mockName("LanguageService.isSelectedLanguageRtl")
        };
        const languageLoadSpy = {
            load: vi.fn().mockName("LanguageLoader.load")
        };
        Object.assign(languageLoadSpy, { languageLoading$: languageLoadingSubject.asObservable() });
        Object.assign(languageSpy, { languageChanged$: languageChangedSubject.asObservable() });

        const routSpy = {
            navigate: vi.fn().mockName("Router.navigate")
        };
        Object.assign(routSpy, { events: new BehaviorSubject<any>(null) });

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

        authServiceSpy = TestBed.inject(AuthenticationService) as MockedObject<AuthenticationService>;
        brandingServiceSpy = TestBed.inject(BrandingService) as MockedObject<BrandingService>;
        chatServiceSpy = TestBed.inject(ChatService) as MockedObject<ChatService>;
        languageServiceSpy = TestBed.inject(LanguageService) as MockedObject<LanguageService>;
        languageLoaderSpy = TestBed.inject(LanguageLoader) as MockedObject<LanguageLoader>;
        routerSpy = TestBed.inject(Router) as MockedObject<Router>;
    }));

    beforeEach(() => {
        originalEnvironmentName = environment.environmentName;
        // Set default return values
        languageServiceSpy.getSelectedLanguage.mockReturnValue('en');
        languageServiceSpy.isSelectedLanguageRtl.mockReturnValue(false);
        authServiceSpy.getTcInstanceType.mockReturnValue(TcInstanceType.TBB);

        fixture = TestBed.createComponent(AppComponent);
        component = fixture.componentInstance;
    });

    afterEach(() => {
        environment.environmentName = originalEnvironmentName;
    });

    it('should create the app', () => {
        expect(component).toBeTruthy();
    });

    describe('Chatbot Visibility Gate', () => {
        it('should show chatbot for GRN instances in staging', fakeAsync(() => {
            environment.environmentName = 'staging';
            authServiceSpy.getTcInstanceType.mockReturnValue(TcInstanceType.GRN);
            fixture.detectChanges();
            tick();

            expect(component.showTcChatbot).toBe(true);
        }));

        it('should hide chatbot for TBB instances in staging', fakeAsync(() => {
            environment.environmentName = 'staging';
            authServiceSpy.getTcInstanceType.mockReturnValue(TcInstanceType.TBB);
            fixture.detectChanges();
            tick();

            expect(component.showTcChatbot).toBe(false);
        }));

        it('should hide chatbot for GRN instances in prod', fakeAsync(() => {
            environment.environmentName = 'prod';
            authServiceSpy.getTcInstanceType.mockReturnValue(TcInstanceType.GRN);
            fixture.detectChanges();
            tick();

            expect(component.showTcChatbot).toBe(false);
        }));

        it('should show chatbot for GRN instances in local', fakeAsync(() => {
            environment.environmentName = 'local';
            authServiceSpy.getTcInstanceType.mockReturnValue(TcInstanceType.GRN);
            fixture.detectChanges();
            tick();

            expect(component.showTcChatbot).toBe(true);
        }));

        it('should recompute chatbot visibility when user login state changes', fakeAsync(() => {
            environment.environmentName = 'staging';
            authServiceSpy.getTcInstanceType.mockReturnValue(TcInstanceType.TBB);
            fixture.detectChanges();
            tick();
            expect(component.showTcChatbot).toBe(false);

            authServiceSpy.getTcInstanceType.mockReturnValue(TcInstanceType.GRN);
            const mockUser: User = {
                id: 1,
                username: 'testuser',
                email: 'test@example.com'
            } as User;

            loggedInUserSubject.next(mockUser);
            tick();

            expect(component.showTcChatbot).toBe(true);
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

        it('should update partner abbreviation when query params change', fakeAsync(() => {
            const mockQueryParamMap = {
                get: (key: string) => key === 'p' ? 'PARTNER' : null
            };

            fixture.detectChanges();
            queryParamMapSubject.next(mockQueryParamMap);
            tick();

            expect(brandingServiceSpy.setPartnerAbbreviation).toHaveBeenCalledWith('PARTNER');
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
            languageServiceSpy.isSelectedLanguageRtl.mockReturnValue(true);

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
