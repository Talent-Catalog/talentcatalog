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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {IeltsLevelComponent} from './ielts-level.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

describe('IeltsLevelComponent', () => {
  let component: IeltsLevelComponent;
  let fixture: ComponentFixture<IeltsLevelComponent>;
  let fb: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IeltsLevelComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule],
      providers: [UntypedFormBuilder]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IeltsLevelComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(UntypedFormBuilder);
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have a form with ieltsLevel control', () => {
    expect(component.form.contains('ieltsLevel')).toBeTrue();
  });

  it('should display error message for required field', () => {
    const compiled = fixture.nativeElement;
    const input = compiled.querySelector('input');
    input.value = '';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    const errorMessage = compiled.querySelector('small').textContent;
    expect(errorMessage).toContain('Must be minimum 4.5 IELTS, average across all bands.');
  });

  it('should display error message for minimum value', () => {
    const compiled = fixture.nativeElement;
    const input = compiled.querySelector('input');
    input.value = '4.0';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    const errorMessage = compiled.querySelector('small').textContent;
    expect(errorMessage).toContain('Must be minimum 4.5 IELTS, average across all bands.');
  });
});
