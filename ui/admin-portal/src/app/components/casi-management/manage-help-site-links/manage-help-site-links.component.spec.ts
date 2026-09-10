import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {of, throwError} from 'rxjs';
import {ManageHelpSiteLinksComponent} from './manage-help-site-links.component';
import {CasiAdminService} from '../../../services/casi-admin.service';

describe('ManageHelpSiteLinksComponent', () => {
  let component: ManageHelpSiteLinksComponent;
  let fixture: ComponentFixture<ManageHelpSiteLinksComponent>;
  let casiAdminService: jasmine.SpyObj<CasiAdminService>;

  beforeEach(async () => {
    casiAdminService = jasmine.createSpyObj('CasiAdminService', [
      'listSharedLinks',
      'createSharedLink',
      'updateSharedLink',
      'disableSharedLink'
    ]);

    casiAdminService.listSharedLinks.and.returnValue(of([]));
    casiAdminService.createSharedLink.and.returnValue(of({} as any));
    casiAdminService.updateSharedLink.and.returnValue(of({} as any));
    casiAdminService.disableSharedLink.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [ManageHelpSiteLinksComponent],
      providers: [{provide: CasiAdminService, useValue: casiAdminService}],
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
    casiAdminService.listSharedLinks.and.returnValue(throwError(() => new Error('oops')));
    component['loadLinks']();

    expect(component.error).toBe('Failed to load shared links.');
    expect(component.links).toEqual([]);
    expect(component.loading).toBeFalse();
  });

  it('should load shared links', () => {
    const links = [{id: 1, countryIsoCode: 'AU', resourceCode: 'https://example.com'}] as any;
    casiAdminService.listSharedLinks.and.returnValue(of(links));

    component['loadLinks']();

    expect(component.links).toEqual(links);
    expect(component.loading).toBeFalse();
  });

  it('should treat a null link list as empty', () => {
    casiAdminService.listSharedLinks.and.returnValue(of(null as any));

    component['loadLinks']();

    expect(component.links).toEqual([]);
    expect(component.loading).toBeFalse();
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

    expect(component.saving).toBeFalse();
    expect(component.editingId).toBeNull();
    expect(component.countryIsoCode).toBe('');
    expect(component.resourceCode).toBe('');
    expect(casiAdminService.listSharedLinks).toHaveBeenCalledTimes(2);
  });

  it('should show error when save fails', () => {
    component.countryIsoCode = 'AU';
    component.resourceCode = 'https://example.com';
    casiAdminService.createSharedLink.and.returnValue(throwError(() => new Error('nope')));

    component.submit();

    expect(component.saving).toBeFalse();
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
    component.edit({id: 7} as any);

    expect(component.editingId).toBe(7);
    expect(component.countryIsoCode).toBe('');
    expect(component.resourceCode).toBe('');
  });

  it('should not disable a link when confirm is cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.remove({id: 1, countryIsoCode: 'AU'} as any);

    expect(casiAdminService.disableSharedLink).not.toHaveBeenCalled();
  });

  it('should disable a link after confirm', () => {
    spyOn(window, 'confirm').and.returnValue(true);

    component.remove({id: 9, countryIsoCode: 'AU'} as any);

    expect(casiAdminService.disableSharedLink)
      .toHaveBeenCalledWith('UNHCR', 'HELP_SITE_LINK', 9);
    expect(component.saving).toBeFalse();
    expect(component.editingId).toBeNull();
  });

  it('should show error when disable fails', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    casiAdminService.disableSharedLink.and.returnValue(throwError(() => new Error('fail')));

    component.remove({id: 9, countryIsoCode: 'AU'} as any);

    expect(component.saving).toBeFalse();
    expect(component.error).toBe('Failed to disable link.');
  });
});
