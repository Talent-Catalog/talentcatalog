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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PotentialDuplicateIconComponent } from './potential-duplicate-icon.component';
import { CandidateService } from "../../../../services/candidate.service";
import { MockCandidate } from "../../../../MockData/MockCandidate";
import { AuthorizationService } from "../../../../services/authorization.service";
import { By } from "@angular/platform-browser";

describe('PotentialDuplicateIconComponent', () => {
  let component: PotentialDuplicateIconComponent;
  let fixture: ComponentFixture<PotentialDuplicateIconComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const mockCandidate = new MockCandidate();

  beforeEach(async () => {
    const candidateService =
      jasmine.createSpyObj('CandidateService', ['fetchPotentialDuplicates']);
    const authorizationService =
      jasmine.createSpyObj('AuthorizationService', ['canViewCandidateName']);

    await TestBed.configureTestingModule({
      declarations: [PotentialDuplicateIconComponent],
      providers: [
        { provide: CandidateService, useValue: candidateService },
        { provide: AuthorizationService, useValue: authorizationService },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PotentialDuplicateIconComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    authorizationServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call canViewName and not display if the response is false, provided the ' +
    'other condition check passes', () => {
    spyOn(component, 'canViewCandidateName').and.returnValue(false);
    component.candidate.potentialDuplicate = true;
    fixture.detectChanges();

    const potentialDuplicateElement =
      fixture.debugElement.query(By.css('span i.fa-person-circle-exclamation'));
    expect(potentialDuplicateElement).toBeNull();
  });

  it('should call canViewName and display if the response is true, provided the other ' +
    'condition check passes', () => {
    authorizationServiceSpy.canViewCandidateName.and.returnValue(true);
    component.candidate.potentialDuplicate = true;
    fixture.detectChanges();

    const potentialDuplicateElement =
      fixture.debugElement.query(By.css('span i.fa-person-circle-exclamation'));
    expect(potentialDuplicateElement).toBeTruthy();
  });

  it('should not be visible when mock candidate is not a potential duplicate', () => {
    const potentialDuplicateElement =
      fixture.debugElement.query(By.css('span i.fa-person-circle-exclamation'));
    expect(potentialDuplicateElement).toBeNull();
  });

  it('should open the modal when clicked, provided condition checks pass', () => {
    spyOn(component, 'openDuplicateDetailModal');
    authorizationServiceSpy.canViewCandidateName.and.returnValue(true);
    component.candidate.potentialDuplicate = true;
    fixture.detectChanges();

    const iconElement =
      fixture.debugElement.nativeElement.querySelector('i.fa-solid.fa-person-circle-exclamation');
    iconElement.click();

    expect(component.openDuplicateDetailModal).toHaveBeenCalled();
  });

});
