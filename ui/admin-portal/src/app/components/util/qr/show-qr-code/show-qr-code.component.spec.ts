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
import {ShowQrCodeComponent} from "./show-qr-code.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule} from "@angular/forms";

describe('ShowQrCodeComponent', () => {
  let component: ShowQrCodeComponent;
  let fixture: ComponentFixture<ShowQrCodeComponent>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close']);

    await TestBed.configureTestingModule({
      declarations: [ ShowQrCodeComponent ],
      imports: [ FormsModule ],  // Add other necessary imports like ReactiveFormsModule if needed
      providers: [
        { provide: NgbActiveModal, useValue: activeModalSpy },
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShowQrCodeComponent);
    component = fixture.componentInstance;

    // Mock data or service responses
    component.qr = { base64Encoding: 'mockedBase64Image' };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should close modal when closeModal() is called', () => {
    component.closeModal();
    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should disable confirm button initially when checkbox is not checked', () => {
    const confirmButton: HTMLButtonElement = fixture.nativeElement.querySelector('.btn-primary');
    expect(confirmButton.disabled).toBeTrue(); // Assuming checkbox is initially unchecked
  });

  it('should enable confirm button when checkbox is checked', () => {
    component.checked = true;
    fixture.detectChanges();
    const confirmButton: HTMLButtonElement = fixture.nativeElement.querySelector('.btn-primary');
    expect(confirmButton.disabled).toBeFalse();
  });

  it('should display QR code image in the template', () => {
    component.qr = { base64Encoding: 'mockedBase64Image' };
    fixture.detectChanges();
    const imgElement: HTMLImageElement = fixture.nativeElement.querySelector('img');
    expect(imgElement).toBeTruthy();
    expect(imgElement.src).toContain('mockedBase64Image');
  });

  it('should toggle checkbox state when clicked', () => {
    const checkbox: HTMLInputElement = fixture.nativeElement.querySelector('input[type="checkbox"]');
    expect(component.checked).toBeFalse(); // Initially unchecked
    checkbox.click(); // Simulate click event
    expect(component.checked).toBeTrue(); // Should be checked after click
  });

  it('should enable confirm button when checkbox is checked', () => {
    component.checked = true;
    fixture.detectChanges();
    const confirmButton: HTMLButtonElement = fixture.nativeElement.querySelector('.btn-primary');
    expect(confirmButton.disabled).toBeFalse(); // Confirm button should be enabled when checkbox is checked
  });

  it('should call closeModal() when confirm button is clicked and checkbox is checked', () => {
    component.checked = true;
    fixture.detectChanges();
    const confirmButton: HTMLButtonElement = fixture.nativeElement.querySelector('.btn-primary');
    spyOn(component, 'closeModal');
    confirmButton.click();
    expect(component.closeModal).toHaveBeenCalled(); // Ensure closeModal() method is called when confirm button is clicked
  });

});
