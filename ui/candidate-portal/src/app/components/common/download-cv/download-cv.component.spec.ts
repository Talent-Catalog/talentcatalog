/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {DownloadCvComponent} from './download-cv.component';
import {CandidateService} from '../../../services/candidate.service';

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() size?: string;
  @Input() color?: string;
  @Input() disabled?: boolean;
  @Output() onClick = new EventEmitter<void>();
}

describe('DownloadCvComponent', () => {
  let component: DownloadCvComponent;
  let fixture: ComponentFixture<DownloadCvComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let windowOpenSpy: jasmine.Spy;
  let createObjectUrlSpy: jasmine.Spy;
  let fakeTab: {location: {href: string}};

  async function configureAndCreate(options?: {downloadError?: unknown}) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['downloadCv']);
    candidateServiceSpy.downloadCv.and.returnValue(
      options?.downloadError ? throwError(options.downloadError) : of(new Blob(['cv']))
    );

    await TestBed.configureTestingModule({
      declarations: [
        DownloadCvComponent,
        TcButtonStubComponent
      ],
      imports: [TranslateModule.forRoot()],
      providers: [
        {provide: CandidateService, useValue: candidateServiceSpy}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DownloadCvComponent);
    component = fixture.componentInstance;
    fakeTab = {location: {href: ''}};
    windowOpenSpy = spyOn(window, 'open').and.returnValue(fakeTab as any);
    createObjectUrlSpy = spyOn(URL, 'createObjectURL').and.returnValue('blob:test');

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();

    expect(component).toBeTruthy();
  });

  describe('template tc components', () => {
    it('should render the download button as a white small tc-button', async () => {
      await configureAndCreate();
      const button = fixture.debugElement.query(By.directive(TcButtonStubComponent));

      expect(button).toBeTruthy();
      expect(button.componentInstance.size).toBe('sm');
      expect(button.componentInstance.color).toBe('white');
    });

    it('should show the loading text when loading is true', async () => {
      await configureAndCreate();
      component.loading = true;
      fixture.detectChanges();

      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('LOADING');
    });
  });

  describe('downloadCV', () => {
    beforeEach(async () => configureAndCreate());

    it('should download the CV and clear loading on success', () => {
      component.downloadCV();

      expect(candidateServiceSpy.downloadCv).toHaveBeenCalled();
      expect(windowOpenSpy).toHaveBeenCalled();
      expect(createObjectUrlSpy).toHaveBeenCalledWith(jasmine.any(Blob));
      expect(fakeTab.location.href).toBe('blob:test');
      expect(component.loading).toBeFalse();
      expect(component.error).toBeUndefined();
    });

    it('should set error and clear loading on failure', () => {
      const serverError = {status: 500};
      candidateServiceSpy.downloadCv.and.returnValue(throwError(serverError));

      component.downloadCV();

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });
  });
});
