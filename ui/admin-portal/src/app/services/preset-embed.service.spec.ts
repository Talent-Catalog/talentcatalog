import { TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi, withXhr } from '@angular/common/http';
import { PresetEmbedService } from './preset-embed.service';

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
