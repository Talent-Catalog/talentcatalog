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

import {Component, Input} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';

import {CandidateCertificationCardComponent} from './candidate-certification-card.component';
import {CandidateCertification} from '../../../model/candidate-certification';

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() color?: string;
  @Input() disabled?: boolean;
}

@Component({
  selector: 'tc-description-list',
  template: '<ng-content></ng-content>'
})
class TcDescriptionListStubComponent {
  @Input() direction?: string;
  @Input() compact?: boolean;
  @Input() size?: string;
}

@Component({
  selector: 'tc-description-item',
  template: '<ng-content></ng-content>'
})
class TcDescriptionItemStubComponent {
  @Input() label?: string;
}

function makeCertification(overrides: Partial<CandidateCertification> = {}): CandidateCertification {
  return {
    id: 1,
    name: 'AWS Practitioner',
    institution: 'Amazon',
    dateCompleted: '2024-01-01',
    ...overrides
  };
}

describe('CandidateCertificationCardComponent', () => {
  let component: CandidateCertificationCardComponent;
  let fixture: ComponentFixture<CandidateCertificationCardComponent>;

  async function configureAndCreate(options?: {
    preview?: boolean;
    disabled?: boolean;
    certificate?: CandidateCertification;
  }) {
    await TestBed.configureTestingModule({
      declarations: [
        CandidateCertificationCardComponent,
        TcButtonStubComponent,
        TcDescriptionListStubComponent,
        TcDescriptionItemStubComponent
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateCertificationCardComponent);
    component = fixture.componentInstance;
    component.preview = options?.preview ?? false;
    component.disabled = options?.disabled ?? false;
    component.certificate = options?.certificate ?? makeCertification();

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template', () => {
    it('should render edit and delete tc-buttons when not in preview mode', async () => {
      await configureAndCreate();

      const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));
      expect(buttons.length).toBe(2);
      expect(buttons[0].componentInstance.color).toBe('error');
      expect(buttons[1].componentInstance.color).toBe('info');
    });

    it('should render the migrated description-list details', async () => {
      await configureAndCreate({preview: true});

      const items = fixture.debugElement.queryAll(By.directive(TcDescriptionItemStubComponent));
      const labels = items.map(debugEl => debugEl.componentInstance.label);
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(labels).toContain('Date');
      expect(labels).toContain('Institution');
      expect(text).toContain('AWS Practitioner');
      expect(text).toContain('Amazon');
    });
  });

  describe('events', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit onEdit with the current certificate', () => {
      const onEditSpy = spyOn(component.onEdit, 'emit');

      component.editCertificate();

      expect(onEditSpy).toHaveBeenCalledWith(component.certificate);
    });

    it('should emit onDelete with the current certificate', () => {
      const onDeleteSpy = spyOn(component.onDelete, 'emit');

      component.deleteCertificate();

      expect(onDeleteSpy).toHaveBeenCalledWith(component.certificate);
    });
  });
});
