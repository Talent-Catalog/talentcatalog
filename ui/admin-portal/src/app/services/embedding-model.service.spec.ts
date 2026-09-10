/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {EmbeddingModelService} from './embedding-model.service';
import {EmbeddingModel} from '../model/embedding-model';
import {environment} from '../../environments/environment';

describe('EmbeddingModelService', () => {
  let service: EmbeddingModelService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/embedding-model';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EmbeddingModelService]
    });
    service = TestBed.inject(EmbeddingModelService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve a list of ready embedding models', () => {
    const mockModels: EmbeddingModel[] = [
      {
        configurationVersion: '1',
        dimensions: 768,
        modelKey: 'model-a',
        modelName: 'Model A',
        modelUrl: 'http://example.com/model-a',
        provider: 'provider-a',
        status: 'READY'
      },
      {
        configurationVersion: '2',
        dimensions: 1536,
        modelKey: 'model-b',
        modelName: 'Model B',
        modelUrl: 'http://example.com/model-b',
        provider: 'provider-b',
        status: 'READY'
      }
    ];

    service.loadReadyModels().subscribe((models) => {
      expect(models.length).toBe(2);
      expect(models).toEqual(mockModels);
    });

    const req = httpMock.expectOne(`${apiUrl}/ready`);
    expect(req.request.method).toBe('GET');
    req.flush(mockModels);
  });

  it('should return an empty array when there are no ready models', () => {
    service.loadReadyModels().subscribe((models) => {
      expect(models).toEqual([]);
    });

    const req = httpMock.expectOne(`${apiUrl}/ready`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
