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

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule, TranslateService} from '@ngx-translate/core';

import {DeleteOccupationComponent} from './delete-occupation.component';

describe('DeleteOccupationComponent', () => {
  let component: DeleteOccupationComponent;
  let fixture: ComponentFixture<DeleteOccupationComponent>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  async function configureAndCreate() {
    activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [DeleteOccupationComponent],
      imports: [TranslateModule.forRoot()],
      providers: [
        {provide: NgbActiveModal, useValue: activeModalSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteOccupationComponent);
    component = fixture.componentInstance;

    const translateService = TestBed.inject(TranslateService);
    translateService.use('en');

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template', () => {
    beforeEach(async () => configureAndCreate());

    it('should render the translated key content for the delete modal', () => {
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('REGISTRATION.OCCUPATION.DELETE.TITLE');
      expect(text).toContain('REGISTRATION.OCCUPATION.DELETE.CONFIRMATION');
      expect(text).toContain('REGISTRATION.OCCUPATION.DELETE.YES');
      expect(text).toContain('REGISTRATION.OCCUPATION.DELETE.NO');
    });

    it('should render the expected close, confirm, and cancel actions', () => {
      const nativeElement = fixture.nativeElement as HTMLElement;
      const text = nativeElement.textContent || '';
      const buttons = nativeElement.querySelectorAll('tc-button');

      expect(buttons.length).toBeGreaterThanOrEqual(3);
      expect(nativeElement.querySelector('tc-button.btn-close')).toBeTruthy();
      expect(text).toContain('REGISTRATION.OCCUPATION.DELETE.YES');
      expect(text).toContain('REGISTRATION.OCCUPATION.DELETE.NO');
    });
  });

  describe('cancel', () => {
    beforeEach(async () => configureAndCreate());

    it('should dismiss the active modal with false', () => {
      component.cancel();

      expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
    });
  });

  describe('confirm', () => {
    beforeEach(async () => configureAndCreate());

    it('should set deleting to true', () => {
      component.confirm();

      expect(component.deleting).toBeTrue();
    });

    it('should close the active modal with true', () => {
      component.confirm();

      expect(activeModalSpy.close).toHaveBeenCalledWith(true);
    });

    it('should render the deleting message when deleting is true', () => {
      component.confirm();
      fixture.detectChanges();

      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('deleting...');
    });
  });
});
