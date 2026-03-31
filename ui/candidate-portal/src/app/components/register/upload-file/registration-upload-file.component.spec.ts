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
import {NgbPopoverModule} from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule, TranslateService} from '@ngx-translate/core';

import {RegistrationUploadFileComponent} from './registration-upload-file.component';
import {RegistrationService} from '../../../services/registration.service';

@Component({
  selector: 'tc-accordion',
  template: '<ng-content></ng-content>'
})
class TcAccordionStubComponent {
  @Input() activeIndexes?: number | null;
  @Input() showOpenCloseAll?: boolean;
}

@Component({
  selector: 'tc-accordion-item',
  template: '<ng-content></ng-content>'
})
class TcAccordionItemStubComponent {}

@Component({
  selector: 'app-candidate-attachments',
  template: ''
})
class CandidateAttachmentsStubComponent {
  @Input() cv?: boolean;
}

@Component({
  selector: 'app-registration-footer',
  template: ''
})
class RegistrationFooterStubComponent {
  @Input() type?: string;
  @Output() backClicked = new EventEmitter<void>();
  @Output() nextClicked = new EventEmitter<void>();
}

describe('RegistrationUploadFileComponent', () => {
  let component: RegistrationUploadFileComponent;
  let fixture: ComponentFixture<RegistrationUploadFileComponent>;
  let registrationServiceSpy: jasmine.SpyObj<RegistrationService>;

  async function configureAndCreate(options?: {edit?: boolean}) {
    registrationServiceSpy = jasmine.createSpyObj('RegistrationService', ['next', 'back']);

    await TestBed.configureTestingModule({
      declarations: [
        RegistrationUploadFileComponent,
        TcAccordionStubComponent,
        TcAccordionItemStubComponent,
        CandidateAttachmentsStubComponent,
        RegistrationFooterStubComponent
      ],
      imports: [
        NgbPopoverModule,
        TranslateModule.forRoot()
      ],
      providers: [
        {provide: RegistrationService, useValue: registrationServiceSpy},
        TranslateService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationUploadFileComponent);
    component = fixture.componentInstance;
    component.edit = options?.edit ?? false;

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();

    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should initialize activeIndexes to 0 when edit is false', async () => {
      await configureAndCreate({edit: false});

      expect(component.activeIndexes).toBe(0);
    });

    it('should initialize activeIndexes to null when edit is true', async () => {
      await configureAndCreate({edit: true});

      expect(component.activeIndexes).toBeNull();
    });
  });

  describe('template', () => {
    beforeEach(async () => configureAndCreate());

    it('should render two accordion sections with candidate attachment children', () => {
      const accordions = fixture.debugElement.queryAll(By.directive(TcAccordionStubComponent));
      const attachmentChildren = fixture.debugElement.queryAll(By.directive(CandidateAttachmentsStubComponent));

      expect(accordions.length).toBe(2);
      expect(attachmentChildren.length).toBe(2);
      expect(attachmentChildren.map(debugEl => debugEl.componentInstance.cv)).toEqual([true, false]);
    });

    it('should render the registration footer with step mode by default', () => {
      const footer = fixture.debugElement.query(By.directive(RegistrationFooterStubComponent));

      expect(footer).toBeTruthy();
      expect(footer.componentInstance.type).toBe('step');
    });

    it('should render the registration footer with update mode when editing', async () => {
      TestBed.resetTestingModule();
      await configureAndCreate({edit: true});

      const footer = fixture.debugElement.query(By.directive(RegistrationFooterStubComponent));

      expect(footer.componentInstance.type).toBe('update');
    });
  });

  describe('navigation', () => {
    beforeEach(async () => configureAndCreate());

    it('should call registrationService.next() on next()', () => {
      component.next();

      expect(registrationServiceSpy.next).toHaveBeenCalled();
    });

    it('should call registrationService.back() on back()', () => {
      component.back();

      expect(registrationServiceSpy.back).toHaveBeenCalled();
    });

    it('should emit onSave on update()', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');

      component.update();

      expect(onSaveSpy).toHaveBeenCalled();
    });

    it('should emit onSave on cancel()', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');

      component.cancel();

      expect(onSaveSpy).toHaveBeenCalled();
    });
  });
});
