import {TestBed} from '@angular/core/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient, withInterceptorsFromDi, withXhr} from '@angular/common/http';
import {PresetEmbedService} from './preset-embed.service';
import {expect, vi} from 'vitest';

describe('PresetEmbedService', () => {
  let service: PresetEmbedService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PresetEmbedService, provideHttpClient(withXhr(), withInterceptorsFromDi()), provideHttpClientTesting()]
    });

    service = TestBed.inject(PresetEmbedService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

// ADMIN_PRESET_SDK_MOCK_20261117: the SDK's browser module is not Node-resolvable.
// Service method behavior can be verified through a separately configured SDK spy.
vi.mock('@preset-sdk/embedded', () => ({embedDashboard: vi.fn()}));
