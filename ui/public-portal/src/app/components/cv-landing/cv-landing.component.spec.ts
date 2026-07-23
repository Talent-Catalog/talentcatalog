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

import {CommonModule} from '@angular/common';
import {Component, Input} from '@angular/core';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {ActivatedRoute, convertToParamMap, ParamMap} from '@angular/router';
import {Subject} from 'rxjs';

import {Candidate} from '../../model/candidate';
import {CvService} from '../../services/cv.service';
import {CvLandingComponent} from './cv-landing.component';

@Component({
  selector: 'app-cv-display',
  template: ''
})
class CvDisplayStubComponent {
  @Input() candidate: Candidate;
}

describe('CvLandingComponent', () => {
  let component: CvLandingComponent;
  let fixture: ComponentFixture<CvLandingComponent>;
  let routeParamMap: Subject<ParamMap>;
  let cvService: jasmine.SpyObj<CvService>;

  const candidate = {
    id: 1,
    candidateNumber: '123456',
    publicId: 'candidate-public-id'
  } as Candidate;

  beforeEach(waitForAsync(() => {
    routeParamMap = new Subject<ParamMap>();
    cvService = jasmine.createSpyObj<CvService>('CvService', [
      'decodeCvRequest'
    ]);

    TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [CvLandingComponent, CvDisplayStubComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {paramMap: routeParamMap.asObservable()}
        },
        {
          provide: CvService,
          useValue: cvService
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CvLandingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch and display the candidate identified by the route token', () => {
    const response = new Subject<Candidate>();
    cvService.decodeCvRequest.and.returnValue(response.asObservable());

    routeParamMap.next(convertToParamMap({token: 'valid-token'}));

    expect(cvService.decodeCvRequest).toHaveBeenCalledOnceWith('valid-token');
    expect(component.loading).toBeTrue();
    expect(component.error).toBeNull();

    response.next(candidate);
    response.complete();
    fixture.detectChanges();

    expect(component.candidate).toEqual(candidate);
    expect(component.loading).toBeFalse();

    const cvDisplay =
      fixture.nativeElement.querySelector('app-cv-display');
    expect(cvDisplay).not.toBeNull();
  });

  it('should expose an error when the CV request fails', () => {
    const response = new Subject<Candidate>();
    const expectedError = 'Unable to load CV';
    cvService.decodeCvRequest.and.returnValue(response.asObservable());

    routeParamMap.next(convertToParamMap({token: 'invalid-token'}));
    response.error(expectedError);
    fixture.detectChanges();

    expect(component.error).toBe(expectedError);
    expect(component.loading).toBeFalse();
    expect(
      fixture.nativeElement.querySelector('.alert-danger').textContent
    ).toContain(expectedError);
  });

  it('should not request a CV when the route has no token', () => {
    routeParamMap.next(convertToParamMap({}));

    expect(cvService.decodeCvRequest).not.toHaveBeenCalled();
  });
});
