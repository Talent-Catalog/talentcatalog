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
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

import {DeleteExamComponent} from './delete-exam.component';

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() color?: string;
  @Input() disabled?: boolean;
  @Output() onClick = new EventEmitter<void>();
}

describe('DeleteExamComponent', () => {
  let component: DeleteExamComponent;
  let fixture: ComponentFixture<DeleteExamComponent>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  async function configureAndCreate() {
    activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [DeleteExamComponent, TcButtonStubComponent],
      imports: [TranslateModule.forRoot()],
      providers: [{provide: NgbActiveModal, useValue: activeModalSpy}]
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteExamComponent);
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

  it('should dismiss the modal on cancel', async () => {
    await configureAndCreate();

    component.cancel();

    expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
  });

  it('should set deleting and close the modal on confirm', async () => {
    await configureAndCreate();

    component.confirm();

    expect(component.deleting).toBeTrue();
    expect(activeModalSpy.close).toHaveBeenCalledWith(true);
  });

  it('should render tc-button actions in the modal footer', async () => {
    await configureAndCreate();
    const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

    expect(buttons.length).toBe(2);
  });
});
