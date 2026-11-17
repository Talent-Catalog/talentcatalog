import type {MockedObject} from "vitest";
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {PresetEmbedComponent} from './preset-embed.component';
import {PresetEmbedService} from '../../../services/preset-embed.service';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import type {EmbeddedDashboard} from '@preset-sdk/embedded';
import {expect, vi} from 'vitest';

describe('PresetEmbedComponent', () => {
    let component: PresetEmbedComponent;
    let fixture: ComponentFixture<PresetEmbedComponent>;
    let presetEmbedService: MockedObject<PresetEmbedService>;
    const mockDashboardId = 'some-dashboard-id';
    const mockDashboard = {
        unmount: vi.fn().mockName("EmbeddedDashboard.unmount")
    };

    beforeEach(async () => {
        // Create a spy for PresetEmbedService
        const spy = {
            embedDashboard: vi.fn().mockName("PresetEmbedService.embedDashboard")
        };

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
        presetEmbedService = TestBed.inject(PresetEmbedService) as MockedObject<PresetEmbedService>;
        component.dashboardId = mockDashboardId;
        presetEmbedService.embedDashboard.mockResolvedValue((mockDashboard as unknown as Awaited<ReturnType<typeof presetEmbedService.embedDashboard>>));

        fixture.detectChanges();
    });

    it('should set error if dashboardId is not provided', () => {
        component.dashboardId = undefined;

        component.ngOnInit();

        expect(component.error)
            .toBe('Dashboard embedding requires a valid dashboard ID from the parent component.');
        expect(component.loading).toBe(false);
    });

    it('should call embedDashboard with the correct dashboardId', () => {
        component.dashboardId = mockDashboardId;

        component.ngOnInit();

        expect(presetEmbedService.embedDashboard).toHaveBeenCalledWith(mockDashboardId, expect.objectContaining({ id: 'dashboard' }));
    });

    it('should set error if embedDashboard fails', async () => {
        const mockError = 'Failed to load dashboard';
        component.dashboardId = mockDashboardId;

        presetEmbedService.embedDashboard.mockRejectedValue(mockError);

        component.ngOnInit();

        await fixture.whenStable();

        // ADMIN_PRESET_AWAIT_FINALLY_20261117: wait for catch/finally, not only fixture stability.
    await vi.waitFor(() => expect(component.loading).toBe(false));
        expect(component.error).toBe(mockError);
    });

});

// ADMIN_PRESET_SDK_MOCK_20261117: component uses the mocked service only.
vi.mock('@preset-sdk/embedded', () => ({embedDashboard: vi.fn()}));
