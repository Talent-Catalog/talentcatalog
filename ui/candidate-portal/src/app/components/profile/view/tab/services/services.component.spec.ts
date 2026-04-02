/*
 * Copyright (c) 2025 Talent Catalog.
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
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {of} from "rxjs";

import { ServicesComponent } from './services.component';
import {ServiceProvider} from "../../../../../model/services";

describe('ServicesComponent', () => {
  let component: ServicesComponent;
  let fixture: ComponentFixture<ServicesComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ServicesComponent],
      schemas: [NO_ERRORS_SCHEMA],
    });
    fixture = TestBed.createComponent(ServicesComponent);
    component = fixture.componentInstance;
    component.showLinkedin$ = of(true);
    component.showReference$ = of(true);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should select reference service', () => {
    component.selectService(ServiceProvider.REFERENCE);
    expect(component.selectedService).toBe(ServiceProvider.REFERENCE);
  });

  describe('selectService', () => {
    it('should set selectedService to LINKEDIN', () => {
      component.selectService(ServiceProvider.LINKEDIN);
      expect(component.selectedService).toBe(ServiceProvider.LINKEDIN);
    });

    it('should set selectedService to DUOLINGO', () => {
      component.selectService(ServiceProvider.DUOLINGO);
      expect(component.selectedService).toBe(ServiceProvider.DUOLINGO);
    });
  });

  describe('onBackButtonClick', () => {
    it('should reset selectedService to null', () => {
      component.selectedService = ServiceProvider.LINKEDIN;
      component.onBackButtonClick();
      expect(component.selectedService).toBeNull();
    });

    it('should emit refresh event', () => {
      spyOn(component.refresh, 'emit');
      component.onBackButtonClick();
      expect(component.refresh.emit).toHaveBeenCalled();
    });
  });

  describe('showLinkedin$ observable', () => {
    it('should accept an observable that emits true', (done) => {
      component.showLinkedin$ = of(true);
      component.showLinkedin$.subscribe(value => {
        expect(value).toBeTrue();
        done();
      });
    });

    it('should accept an observable that emits false', (done) => {
      component.showLinkedin$ = of(false);
      component.showLinkedin$.subscribe(value => {
        expect(value).toBeFalse();
        done();
      });
    });
  });
});
