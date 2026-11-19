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
import {SearchHelpLinksComponent} from "./search-help-links.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HelpLinkService} from "../../../services/help-link.service";
import {CountryService} from "../../../services/country.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {SearchResults} from "../../../model/search-results";
import MOCK_HELP_LINK from "../../../MockData/MockHelpLink";
import {HelpLink} from "../../../model/help-link";
import {HelpComponent} from "../../help/help.component";
import {CreateUpdateHelpLinkComponent} from "./create-update-help-link/create-update-help-link.component";
import {By} from "@angular/platform-browser";
import {expect, vi} from 'vitest';

describe('SearchHelpLinksComponent', () => {
    let component: SearchHelpLinksComponent;
    let fixture: ComponentFixture<SearchHelpLinksComponent>;
    let helpLinkServiceSpy: MockedObject<HelpLinkService>;
    let countryServiceSpy: MockedObject<CountryService>;
    let modalServiceSpy: MockedObject<NgbModal>;
    let authServiceSpy: MockedObject<AuthorizationService>;
    const searchResults: SearchResults<HelpLink> = {
        first: false,
        last: false,
        number: 0,
        size: 0,
        totalPages: 0,
        totalElements: 1,
        content: [MOCK_HELP_LINK]
    };

    beforeEach(async () => {
        const helpLinkServiceMock = {
            searchPaged: vi.fn().mockName("HelpLinkService.searchPaged")
        };
        const countryServiceMock = {
            listTCDestinations: vi.fn().mockName("CountryService.listTCDestinations")
        };
        const modalServiceMock = {
            open: vi.fn().mockName("NgbModal.open")
        };
        const authServiceMock = {
            isReadOnly: vi.fn().mockName("AuthorizationService.isReadOnly")
        };

        await TestBed.configureTestingModule({
            declarations: [SearchHelpLinksComponent, HelpComponent],
            imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
            providers: [
                UntypedFormBuilder,
                { provide: HelpLinkService, useValue: helpLinkServiceMock },
                { provide: CountryService, useValue: countryServiceMock },
                { provide: NgbModal, useValue: modalServiceMock },
                { provide: AuthorizationService, useValue: authServiceMock }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(SearchHelpLinksComponent);
        component = fixture.componentInstance;
        helpLinkServiceSpy = TestBed.inject(HelpLinkService) as MockedObject<HelpLinkService>;
        countryServiceSpy = TestBed.inject(CountryService) as MockedObject<CountryService>;
        modalServiceSpy = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
        authServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;

        // Mock initial data and methods
        helpLinkServiceSpy.searchPaged.mockReturnValue(of(searchResults));
        countryServiceSpy.listTCDestinations.mockReturnValue(of([]));
        authServiceSpy.isReadOnly.mockReturnValue(false);

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });


    it('should handle search results', () => {

        // helpLinkServiceSpy.searchPaged.and.returnValue(of(searchResults));

        component.search();

        expect(component.results).toEqual(searchResults);
        expect(component.loading).toBe(false);
    });

    it('should handle search errors', () => {
        const error = 'An error occurred';
        helpLinkServiceSpy.searchPaged.mockReturnValue(throwError(error));

        component.search();

        expect(component.error).toEqual(error);
        expect(component.loading).toBe(false);
    });

    it('should open add help link modal', () => {
        const modalRefMock = {
            componentInstance: { destinationCountries: undefined },
            result: Promise.resolve(true)
        } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);

        component.addHelpLink();

        expect(modalServiceSpy.open).toHaveBeenCalledWith(CreateUpdateHelpLinkComponent, {
            centered: true,
            backdrop: 'static'
        });
    });

    it('should open edit help link modal', () => {
        const modalRefMock = {
            componentInstance: {
                helpLink: null,
                destinationCountries: []
            },
            result: Promise.resolve(true)
        } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);

        const helpLink = { id: 1, label: 'Test Link' } as HelpLink;
        component.editHelpLink(helpLink);

        expect(modalServiceSpy.open).toHaveBeenCalledWith(CreateUpdateHelpLinkComponent, {
            centered: true,
            backdrop: 'static'
        });
        expect(modalRefMock.componentInstance.helpLink).toEqual(helpLink);
    });

    it('should display error message', () => {
        const error = 'An error occurred';
        component.error = error;
        fixture.detectChanges();

        const errorMessage = fixture.debugElement.query(By.css('tc-alert'));
        expect(errorMessage).toBeTruthy();
        expect(errorMessage.nativeElement.textContent).toContain(error);
    });

});
