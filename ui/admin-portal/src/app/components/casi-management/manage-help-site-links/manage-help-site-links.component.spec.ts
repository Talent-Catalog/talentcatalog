import type {MockedObject} from "vitest";
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {of, throwError} from 'rxjs';
import {ManageHelpSiteLinksComponent} from './manage-help-site-links.component';
import {CasiAdminService} from '../../../services/casi-admin.service';
import {expect, vi} from 'vitest';

describe('ManageHelpSiteLinksComponent', () => {
    let component: ManageHelpSiteLinksComponent;
    let fixture: ComponentFixture<ManageHelpSiteLinksComponent>;
    let casiAdminService: MockedObject<CasiAdminService>;

    beforeEach(async () => {
        casiAdminService = ({
            listSharedLinks: vi.fn().mockName("CasiAdminService.listSharedLinks"),
            createSharedLink: vi.fn().mockName("CasiAdminService.createSharedLink"),
            updateSharedLink: vi.fn().mockName("CasiAdminService.updateSharedLink"),
            disableSharedLink: vi.fn().mockName("CasiAdminService.disableSharedLink")
        } as unknown as typeof casiAdminService);

        casiAdminService.listSharedLinks.mockReturnValue(of([]));
        casiAdminService.createSharedLink.mockReturnValue(of({} as any));
        casiAdminService.updateSharedLink.mockReturnValue(of({} as any));
        casiAdminService.disableSharedLink.mockReturnValue(of(void 0));

        await TestBed.configureTestingModule({
            imports: [FormsModule],
            declarations: [ManageHelpSiteLinksComponent],
            providers: [{ provide: CasiAdminService, useValue: casiAdminService }],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        fixture = TestBed.createComponent(ManageHelpSiteLinksComponent);
        component = fixture.componentInstance;
        component.provider = 'UNHCR';
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
        expect(casiAdminService.listSharedLinks).toHaveBeenCalledWith('UNHCR', 'HELP_SITE_LINK');
    });

    it('should create a shared link', () => {
        component.countryIsoCode = 'au';
        component.resourceCode = 'https://example.com';

        component.submit();

        expect(casiAdminService.createSharedLink)
            .toHaveBeenCalledWith('UNHCR', 'HELP_SITE_LINK', 'AU', 'https://example.com');
    });

    it('should update a shared link in edit mode', () => {
        component.editingId = 10;
        component.countryIsoCode = 'pk';
        component.resourceCode = 'https://help.example.com';

        component.submit();

        expect(casiAdminService.updateSharedLink)
            .toHaveBeenCalledWith('UNHCR', 'HELP_SITE_LINK', 10, 'PK', 'https://help.example.com');
    });

    it('should show error on load failure', () => {
        casiAdminService.listSharedLinks.mockReturnValue(throwError(() => new Error('oops')));
        component['loadLinks']();

        expect(component.error).toBe('Failed to load shared links.');
        expect(component.links).toEqual([]);
        expect(component.loading).toBe(false);
    });

    it('should load shared links', () => {
        const links = [{ id: 1, countryIsoCode: 'AU', resourceCode: 'https://example.com' }] as any;
        casiAdminService.listSharedLinks.mockReturnValue(of(links));

        component['loadLinks']();

        expect(component.links).toEqual(links);
        expect(component.loading).toBe(false);
    });

    it('should treat a null link list as empty', () => {
        casiAdminService.listSharedLinks.mockReturnValue(of(null as any));

        component['loadLinks']();

        expect(component.links).toEqual([]);
        expect(component.loading).toBe(false);
    });

    it('should require country and URL before saving', () => {
        component.countryIsoCode = '  ';
        component.resourceCode = '';

        component.submit();

        expect(component.error).toBe('Country and link URL are required.');
        expect(casiAdminService.createSharedLink).not.toHaveBeenCalled();
        expect(casiAdminService.updateSharedLink).not.toHaveBeenCalled();
    });

    it('should reset the form after a successful create', () => {
        component.countryIsoCode = 'au';
        component.resourceCode = 'https://example.com';

        component.submit();

        expect(component.saving).toBe(false);
        expect(component.editingId).toBeNull();
        expect(component.countryIsoCode).toBe('');
        expect(component.resourceCode).toBe('');
        expect(casiAdminService.listSharedLinks).toHaveBeenCalledTimes(2);
    });

    it('should show error when save fails', () => {
        component.countryIsoCode = 'AU';
        component.resourceCode = 'https://example.com';
        casiAdminService.createSharedLink.mockReturnValue(throwError(() => new Error('nope')));

        component.submit();

        expect(component.saving).toBe(false);
        expect(component.error).toBe('Failed to save link. Please check country code and URL.');
    });

    it('should populate the form for edit and cancel edit', () => {
        component.edit({
            id: 5,
            countryIsoCode: 'AU',
            resourceCode: 'https://example.com'
        } as any);

        expect(component.editingId).toBe(5);
        expect(component.countryIsoCode).toBe('AU');
        expect(component.resourceCode).toBe('https://example.com');

        component.cancelEdit();

        expect(component.editingId).toBeNull();
        expect(component.countryIsoCode).toBe('');
        expect(component.resourceCode).toBe('');
    });

    it('should default missing edit fields to empty strings', () => {
        component.edit({ id: 7 } as any);

        expect(component.editingId).toBe(7);
        expect(component.countryIsoCode).toBe('');
        expect(component.resourceCode).toBe('');
    });

    it('should not disable a link when confirm is cancelled', () => {
        vi.spyOn(window, 'confirm').mockReturnValue(false);

        component.remove({ id: 1, countryIsoCode: 'AU' } as any);

        expect(casiAdminService.disableSharedLink).not.toHaveBeenCalled();
    });

    it('should disable a link after confirm', () => {
        vi.spyOn(window, 'confirm').mockReturnValue(true);

        component.remove({ id: 9, countryIsoCode: 'AU' } as any);

        expect(casiAdminService.disableSharedLink)
            .toHaveBeenCalledWith('UNHCR', 'HELP_SITE_LINK', 9);
        expect(component.saving).toBe(false);
        expect(component.editingId).toBeNull();
    });

    it('should show error when disable fails', () => {
        vi.spyOn(window, 'confirm').mockReturnValue(true);
        casiAdminService.disableSharedLink.mockReturnValue(throwError(() => new Error('fail')));

        component.remove({ id: 9, countryIsoCode: 'AU' } as any);

        expect(component.saving).toBe(false);
        expect(component.error).toBe('Failed to disable link.');
    });
});
