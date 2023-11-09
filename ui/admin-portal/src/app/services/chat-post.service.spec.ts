import {TestBed} from '@angular/core/testing';

import {ChatPostService} from './chat-post.service';

describe('ChatPostService', () => {
  let service: ChatPostService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ChatPostService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
