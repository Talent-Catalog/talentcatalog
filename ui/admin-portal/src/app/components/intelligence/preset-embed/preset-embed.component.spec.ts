import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PresetEmbedComponent } from './preset-embed.component';
import { PresetEmbedService } from '../../../services/preset-embed.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import {of, throwError} from "rxjs";

fdescribe('PresetEmbedComponent', () => {
  let component: PresetEmbedComponent;
  let fixture: ComponentFixture<PresetEmbedComponent>;
  let presetEmbedService: jasmine.SpyObj<PresetEmbedService>;
  const mockDashboardId = 'some-dashboard-id';

  beforeEach(async () => {
    // Create a spy for PresetEmbedService
    const spy =
      jasmine.createSpyObj('PresetEmbedService', ['embedDashboard']);

    // Set up the TestBed with the spy
    await TestBed.configureTestingModule({
      declarations: [PresetEmbedComponent],
      providers: [
        { provide: PresetEmbedService, useValue: spy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PresetEmbedComponent);
    component = fixture.componentInstance;
    presetEmbedService = TestBed.inject(PresetEmbedService) as jasmine.SpyObj<PresetEmbedService>;

    presetEmbedService.embedDashboard.and.returnValue(of(undefined)); // Mock success

    fixture.detectChanges();
  });

  it('should set error if dashboardId is not provided', () => {
    component.ngOnInit();

    expect(component.error).toBe('Dashboard ID is required');
    expect(component.loading).toBeFalse();
  });

  it('should call embedDashboard with the correct dashboardId', () => {
    const mockDashboardId = 'some-dashboard-id';

    component.dashboardId = mockDashboardId;

    component.ngOnInit();

    expect(presetEmbedService.embedDashboard).toHaveBeenCalledWith(
      mockDashboardId,
      jasmine.objectContaining({ id: 'dashboard' }));
  });

  it('should set error if embedDashboard fails', () => {
    const mockError = 'Failed to load dashboard';

    presetEmbedService.embedDashboard.and.returnValue(throwError(mockError)); // Set error behavior

    component.dashboardId = mockDashboardId;
    component.ngOnInit();

    expect(component.loading).toBeFalse();
    expect(component.error).toBe(mockError);
  });
});
