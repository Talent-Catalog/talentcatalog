import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PresetEmbedComponent } from './preset-embed.component';
import { PresetEmbedService } from '../../../services/preset-embed.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import {EmbeddedDashboard} from "@preset-sdk/embedded";

describe('PresetEmbedComponent', () => {
  let component: PresetEmbedComponent;
  let fixture: ComponentFixture<PresetEmbedComponent>;
  let presetEmbedService: jasmine.SpyObj<PresetEmbedService>;
  const mockDashboardId = 'some-dashboard-id';
  const mockDashboard =
    jasmine.createSpyObj<EmbeddedDashboard>('EmbeddedDashboard', ['unmount']);

  beforeEach(async () => {
    // Create a spy for PresetEmbedService
    const spy =
      jasmine.createSpyObj('PresetEmbedService', ['embedDashboard']);

    // Set up the TestBed with the spy
    await TestBed.configureTestingModule({
      declarations: [PresetEmbedComponent],
      providers: [{ provide: PresetEmbedService, useValue: spy }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PresetEmbedComponent);
    component = fixture.componentInstance;
    presetEmbedService = TestBed.inject(PresetEmbedService) as jasmine.SpyObj<PresetEmbedService>;
    component.dashboardId = mockDashboardId;
    presetEmbedService.embedDashboard.and.returnValue(Promise.resolve(mockDashboard));

    fixture.detectChanges();
  });

  it('should set error if dashboardId is not provided', () => {
    component.dashboardId = undefined;

    component.ngOnInit();

    expect(component.error)
      .toBe('Dashboard embedding requires a valid dashboard ID from the parent component.');
    expect(component.loading).toBeFalse();
  });

  it('should call embedDashboard with the correct dashboardId', () => {
    component.dashboardId = mockDashboardId;

    component.ngOnInit();

    expect(presetEmbedService.embedDashboard).toHaveBeenCalledWith(
      mockDashboardId,
      jasmine.objectContaining({ id: 'dashboard' }));
  });

  it('should set error if embedDashboard fails', async () => {
    const mockError = 'Failed to load dashboard';
    component.dashboardId = mockDashboardId;

    presetEmbedService.embedDashboard.and.returnValue(Promise.reject(mockError));

    component.ngOnInit();

    await fixture.whenStable();

    expect(component.loading).toBeFalse();
    expect(component.error).toBe(mockError);
  });

});
