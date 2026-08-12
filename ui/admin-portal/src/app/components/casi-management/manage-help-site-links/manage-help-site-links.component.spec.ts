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
  });
});
