import { TestBed } from '@angular/core/testing';

import { WebScraperService } from './web-scraper.service';

describe('WebScraperService', () => {
  let service: WebScraperService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WebScraperService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
