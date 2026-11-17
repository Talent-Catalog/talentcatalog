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
import {CandidateSourceResultsComponent} from "./candidate-source-results.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateSourceResultsCacheService} from "../../../../services/candidate-source-results-cache.service";
import {CandidateSourceCandidateService} from "../../../../services/candidate-source-candidate.service";
import {AuthorizationService} from "../../../../services/authorization.service";
import {CandidateFieldService} from "../../../../services/candidate-field.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Router} from "@angular/router";
import {NO_ERRORS_SCHEMA, SimpleChange} from '@angular/core';
import {of} from 'rxjs';
import {DtoType} from '../../../../model/base';
import {expect, vi} from 'vitest';

describe('CandidateSourceResultsComponent', () => {
    let component: CandidateSourceResultsComponent;
    let fixture: ComponentFixture<CandidateSourceResultsComponent>;

    let router: MockedObject<Router>;
    let authorizationService: MockedObject<AuthorizationService>;
    let cacheService: MockedObject<CandidateSourceResultsCacheService>;
    let sourceCandidateService: MockedObject<CandidateSourceCandidateService>;
    let candidateFieldService: MockedObject<CandidateFieldService>;
    let modalService: MockedObject<NgbModal>;

    const savedSearch = {
        id: 10,
        name: 'Test search',
        defaultSearch: false, //Any defaultSearch value makes it look like a saved search
        reviewable: false,
        users: []
    } as any;

    const savedList = {
        id: 20,
        name: 'Test list',
        users: []
    } as any;

    beforeEach(async () => {
        router = ({
            navigate: vi.fn().mockName("Router.navigate")
        } as unknown as typeof router);

        authorizationService = ({
            canViewCandidateName: vi.fn().mockName("AuthorizationService.canViewCandidateName"),
            canViewCandidateCountry: vi.fn().mockName("AuthorizationService.canViewCandidateCountry"),
            canAccessSalesforce: vi.fn().mockName("AuthorizationService.canAccessSalesforce"),
            canAccessGoogleDrive: vi.fn().mockName("AuthorizationService.canAccessGoogleDrive")
        } as unknown as typeof authorizationService);

        cacheService = ({
            getFromCache: vi.fn().mockName("CandidateSourceResultsCacheService.getFromCache"),
            cache: vi.fn().mockName("CandidateSourceResultsCacheService.cache")
        } as unknown as typeof cacheService);

        sourceCandidateService = ({
            searchPaged: vi.fn().mockName("CandidateSourceCandidateService.searchPaged")
        } as unknown as typeof sourceCandidateService);

        candidateFieldService = ({
            getCandidateSourceFields: vi.fn().mockName("CandidateFieldService.getCandidateSourceFields")
        } as unknown as typeof candidateFieldService);

        modalService = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof modalService);

        candidateFieldService.getCandidateSourceFields.mockReturnValue([]);
        sourceCandidateService.searchPaged.mockReturnValue(of({
            content: [],
            totalElements: 0
        } as any));

        await TestBed.configureTestingModule({
            declarations: [CandidateSourceResultsComponent],
            providers: [
                { provide: Router, useValue: router },
                { provide: AuthorizationService, useValue: authorizationService },
                { provide: CandidateSourceResultsCacheService, useValue: cacheService },
                { provide: CandidateSourceCandidateService, useValue: sourceCandidateService },
                { provide: CandidateFieldService, useValue: candidateFieldService },
                { provide: NgbModal, useValue: modalService }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        })
            .overrideTemplate(CandidateSourceResultsComponent, '')
            .compileComponents();

        fixture = TestBed.createComponent(CandidateSourceResultsComponent);
        component = fixture.componentInstance;
        component.candidateSource = savedSearch;

        fixture.detectChanges();
    });


    it('should create and initialize short format', () => {
        component.longFormat = true;

        component.ngOnInit();

        expect(component).toBeTruthy();
        expect(component.longFormat).toBe(false);
    });

    describe('ngOnChanges', () => {
        it('should load fields and search when candidate source changes', () => {
            const loadFieldsSpy = vi.spyOn((component as any), 'loadSelectedFields').mockReturnValue(undefined);
            const searchSpy = vi.spyOn(component, 'search').mockReturnValue(undefined);

            component.ngOnChanges({
                candidateSource: new SimpleChange(savedList, savedSearch, false)
            });

            expect(loadFieldsSpy).toHaveBeenCalledTimes(1);
            expect(searchSpy).toHaveBeenCalledTimes(1);
            expect(searchSpy).toHaveBeenCalledWith(false);
        });

        it('should do nothing when candidate source change is absent', () => {
            const loadFieldsSpy = vi.spyOn((component as any), 'loadSelectedFields').mockReturnValue(undefined);
            const searchSpy = vi.spyOn(component, 'search').mockReturnValue(undefined);

            component.ngOnChanges({});

            expect(loadFieldsSpy).not.toHaveBeenCalled();
            expect(searchSpy).not.toHaveBeenCalled();
        });

        it('should do nothing when previous and current values are identical', () => {
            const loadFieldsSpy = vi.spyOn((component as any), 'loadSelectedFields').mockReturnValue(undefined);
            const searchSpy = vi.spyOn(component, 'search').mockReturnValue(undefined);

            component.ngOnChanges({
                candidateSource: new SimpleChange(savedSearch, savedSearch, false)
            });

            expect(loadFieldsSpy).not.toHaveBeenCalled();
            expect(searchSpy).not.toHaveBeenCalled();
        });
    });

    describe('onOpenSource', () => {
        it('should navigate without query parameters from page one', () => {
            component.pageNumber = 1;
            component.candidateSource = savedSearch;

            component.onOpenSource();

            expect(router.navigate).toHaveBeenCalledWith(['search', savedSearch.id], {});
        });

        it('should preserve the current page number after page one', () => {
            component.pageNumber = 4;
            component.candidateSource = savedList;

            component.onOpenSource();

            expect(router.navigate).toHaveBeenCalledWith(['list', savedList.id], {
                queryParams: {
                    pageNumber: 4
                }
            });
        });
    });

    describe('search', () => {
        it('should stop when cached results are available', () => {
            const checkCacheSpy = vi.spyOn((component as any), 'checkCache').mockReturnValue(true);
            const performSearchSpy = vi.spyOn((component as any), 'performSearch').mockReturnValue(of({}));

            component.search(false);

            expect(checkCacheSpy).toHaveBeenCalledWith(false, false);
            expect(performSearchSpy).not.toHaveBeenCalled();
        });

        it('should perform a preview search when cache is unavailable', () => {
            const checkCacheSpy = vi.spyOn((component as any), 'checkCache').mockReturnValue(false);
            const performSearchSpy = vi.spyOn((component as any), 'performSearch').mockReturnValue(of({}));

            component.search(true);

            expect(checkCacheSpy).toHaveBeenCalledWith(true, false);
            expect(performSearchSpy).toHaveBeenCalledWith(12, DtoType.PREVIEW);
        });
    });

    it('should toggle sorting and refresh the search', () => {
        component.sortField = 'name';
        component.sortDirection = 'ASC';

        const searchSpy = vi.spyOn(component, 'search').mockReturnValue(undefined);

        component.toggleSort('name');

        expect(component.sortDirection).toBe('DESC');
        expect(searchSpy).toHaveBeenCalledTimes(1);
        expect(searchSpy).toHaveBeenCalledWith(true);
    });

    it('should emit toggleStarred', () => {
        const emitSpy = vi.spyOn(component.toggleStarred, 'emit').mockReturnValue(undefined);

        component.onToggleStarred(savedSearch);

        expect(emitSpy).toHaveBeenCalledTimes(1);

        expect(emitSpy).toHaveBeenCalledWith(savedSearch);
    });

    it('should emit toggleWatch', () => {
        const emitSpy = vi.spyOn(component.toggleWatch, 'emit').mockReturnValue(undefined);

        component.onToggleWatch(savedSearch);

        expect(emitSpy).toHaveBeenCalledTimes(1);

        expect(emitSpy).toHaveBeenCalledWith(savedSearch);
    });

    it('should emit deleteSource', () => {
        const emitSpy = vi.spyOn(component.deleteSource, 'emit').mockReturnValue(undefined);

        component.onDeleteSource(savedSearch);

        expect(emitSpy).toHaveBeenCalledTimes(1);

        expect(emitSpy).toHaveBeenCalledWith(savedSearch);
    });

    it('should emit editSource', () => {
        const emitSpy = vi.spyOn(component.editSource, 'emit').mockReturnValue(undefined);

        component.onEditSource(savedSearch);

        expect(emitSpy).toHaveBeenCalledTimes(1);

        expect(emitSpy).toHaveBeenCalledWith(savedSearch);
    });

    it('should emit copySource', () => {
        const emitSpy = vi.spyOn(component.copySource, 'emit').mockReturnValue(undefined);

        component.onCopySource(savedSearch);

        expect(emitSpy).toHaveBeenCalledTimes(1);

        expect(emitSpy).toHaveBeenCalledWith(savedSearch);
    });

    it('should refresh after a page change', () => {
        const searchSpy = vi.spyOn(component, 'search').mockReturnValue(undefined);

        component.onPageChange();

        expect(searchSpy).toHaveBeenCalledTimes(1);

        expect(searchSpy).toHaveBeenCalledWith(true);
    });

    it('should refresh when refresh is requested', () => {
        const searchSpy = vi.spyOn(component, 'search').mockReturnValue(undefined);

        component.onRefreshRequest();

        expect(searchSpy).toHaveBeenCalledTimes(1);

        expect(searchSpy).toHaveBeenCalledWith(true);
    });
});
