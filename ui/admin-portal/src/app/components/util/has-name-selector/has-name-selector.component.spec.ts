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
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NgSelectComponent, NgSelectModule} from '@ng-select/ng-select';
import {HasNameSelectorComponent} from './has-name-selector.component';
import {HasName} from '../../../model/base';

describe('HasNameSelectorComponent', () => {
  let component: HasNameSelectorComponent;
  let fixture: ComponentFixture<HasNameSelectorComponent>;
  let activeModal: jasmine.SpyObj<NgbActiveModal>;

  const mockHasNames: HasName[] = [
    { name: 'Name1' },
    { name: 'Name2' },
    { name: 'Name3' }
  ];

  beforeEach(async () => {
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, NgSelectModule],
      declarations: [HasNameSelectorComponent],
      providers: [
        { provide: NgbActiveModal, useValue: activeModalSpy }
      ]
    }).compileComponents();

    activeModal = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HasNameSelectorComponent);
    component = fixture.componentInstance;
    component.hasNames = mockHasNames;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.form).toBeDefined();
    expect(component.form.value).toEqual({
      hasNameIndex: null
    });
  });

  it('should close the modal with selected name on select', () => {
    component.form.controls['hasNameIndex'].setValue(1);
    component.onSelect();
    expect(activeModal.close).toHaveBeenCalledWith(mockHasNames[1]);
  });

  it('should close the modal with null if no name is selected', () => {
    component.onSelect();
    expect(activeModal.close).toHaveBeenCalledWith(null);
  });

  it('should dismiss the modal on cancel', () => {
    component.onCancel();
    expect(activeModal.dismiss).toHaveBeenCalled();
  });

  it('should display the correct options in the select dropdown', fakeAsync(() => {
    fixture.detectChanges();
    const selectComponent = fixture.debugElement.query(By.directive(NgSelectComponent)).componentInstance as NgSelectComponent;

    // Open the dropdown programmatically
    selectComponent.open();
    tick();  // Advance the virtual clock

    fixture.detectChanges();
    tick();  // Ensure the dropdown options are fully rendered

    // Check if the dropdown panel is rendered
    const dropdownPanel = fixture.debugElement.query(By.css('ng-dropdown-panel'));
    expect(dropdownPanel).not.toBeNull();

    // Ensure the options are being rendered
    const options = dropdownPanel.queryAll(By.css('.ng-option'));
    expect(options.length).toBe(mockHasNames.length);

    options.forEach((option, index) => {
      expect(option.nativeElement.textContent.trim()).toBe(mockHasNames[index].name);
    });
  }));

  it('should emit correct selected value on select', () => {
    const selectButton = fixture.debugElement.query(By.css('.btn-primary')).nativeElement;
    component.form.controls['hasNameIndex'].setValue(2);
    selectButton.click();
    fixture.detectChanges();
    expect(activeModal.close).toHaveBeenCalledWith(mockHasNames[2]);
  });

  it('should call onCancel method when cancel button is clicked', () => {
    spyOn(component, 'onCancel');
    const cancelButton = fixture.debugElement.query(By.css('.btn-secondary')).nativeElement;
    cancelButton.click();
    expect(component.onCancel).toHaveBeenCalled();
  });

  it('should call onSelect method when select button is clicked', () => {
    spyOn(component, 'onSelect');
    const selectButton = fixture.debugElement.query(By.css('.btn-primary')).nativeElement;
    selectButton.click();
    expect(component.onSelect).toHaveBeenCalled();
  });

});
