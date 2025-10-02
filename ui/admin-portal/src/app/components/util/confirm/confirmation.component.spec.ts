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
import {ConfirmationComponent} from "./confirmation.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {By} from "@angular/platform-browser";
import {TcModalComponent} from "../../../shared/components/modal/tc-modal.component";

fdescribe('ConfirmationComponent', () => {
  let component: ConfirmationComponent;
  let fixture: ComponentFixture<ConfirmationComponent>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [ConfirmationComponent, TcModalComponent],
      providers: [
        { provide: NgbActiveModal, useValue: spy }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmationComponent);
    component = fixture.componentInstance;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have default title as "Confirmation"', () => {
    expect(component.title).toBe('Confirmation');
  });

  it('should call dismiss method of NgbActiveModal when dismiss is called', () => {
    component.dismiss();
    expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
  });

  it('should call close method of NgbActiveModal when close is called', () => {
    component.close();
    expect(activeModalSpy.close).toHaveBeenCalledWith(true);
  });

  it('should display the header in the modal header', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.tc-modal-header').textContent).toContain('Confirmation');
  });

  it('should display the message if provided', () => {
    component.message = 'Test message';
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.tc-modal-body span').textContent).toContain('Test message');
  });

  it('should display "Are you sure?" if message is not provided', () => {
    component.message = null;
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.tc-modal-body span').textContent).toContain('Are you sure?');
  });

  it('should display the cancel button if showCancel is true', () => {
    component.showCancel = true;
    fixture.detectChanges();
    const cancelButton = fixture.debugElement.query(By.css('.tc-modal-footer tc-button:nth-child(1)'));
    expect(cancelButton.nativeElement.textContent).toContain('Cancel');
  });

  it('should not display the cancel button if showCancel is false', () => {
    component.showCancel = false;
    fixture.detectChanges();
    const cancelButton = fixture.debugElement.query(By.css('.tc-modal-footer tc-button:nth-child(2)'));
    expect(cancelButton).toBeNull();
  });
});
