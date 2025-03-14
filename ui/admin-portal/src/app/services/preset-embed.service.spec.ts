import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PresetEmbedService } from './preset-embed.service';

describe('PresetEmbedService', () => {
  let service: PresetEmbedService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PresetEmbedService]
    });

    service = TestBed.inject(PresetEmbedService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
