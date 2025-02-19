import { TestBed } from '@angular/core/testing';

import { PresetEmbedService } from './preset-embed.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {environment} from "../../environments/environment";
import {of} from "rxjs";

describe('PresetEmbedService',
  () => {
    let service: PresetEmbedService;
    let httpMock: HttpTestingController;
    let embedDashboardSpy: jasmine.Spy;
    let fetchGuestTokenSpy: jasmine.Spy;
    const mockDashboardId: string = 'dksljf78oi&^Rghk';
    const mockGuestToken: string = 'sdfaghjRDFSD69-s'

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [PresetEmbedService]
      });

      service = TestBed.inject(PresetEmbedService);
      httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
      httpMock.verify();
    });

    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should fetch a guest token', () => {
      // Call the method
      service.fetchGuestToken(mockDashboardId).subscribe(token => {
        // Assert the token returned is correct
        expect(token).toBe(mockGuestToken);
      });

      // Set up the mock request
      const req =
        httpMock.expectOne(`${environment.apiUrl}/preset/${mockDashboardId}/guest-token`);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBeNull();

      req.flush(mockGuestToken);
      httpMock.verify();
    })


    it('should call fetchGuestToken() with the correct dashboardId',
      (done) => {
        const mockElement = document.createElement("div");

        // Spy on fetchGuestToken and return a mock token
        const fetchGuestTokenSpy =
          spyOn(service, 'fetchGuestToken').and.returnValue(of(mockGuestToken));

        service.embedDashboard(mockDashboardId, mockElement)
        .subscribe(() => {
          // Ensure fetchGuestToken was called with the correct dashboardId
          expect(fetchGuestTokenSpy).toHaveBeenCalledWith(mockDashboardId);
          done(); // Ensure async test completes
        });
    });

  });
