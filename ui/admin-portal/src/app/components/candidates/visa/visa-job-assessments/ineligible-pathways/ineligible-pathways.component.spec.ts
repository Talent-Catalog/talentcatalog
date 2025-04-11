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
import {IneligiblePathwaysComponent} from './ineligible-pathways.component';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('IneligiblePathwaysComponent', () => {
  let component: IneligiblePathwaysComponent;
  let fixture: ComponentFixture<IneligiblePathwaysComponent>;
  let fb: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IneligiblePathwaysComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule],
      providers: [UntypedFormBuilder]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IneligiblePathwaysComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(UntypedFormBuilder);
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have a form with visaJobIneligiblePathways control', () => {
    expect(component.form.contains('visaJobIneligiblePathways')).toBeTrue();
  });

  it('should update form value when input changes', () => {
    const testValue = 'Test ineligible pathways';
    const textarea = fixture.nativeElement.querySelector('textarea');
    textarea.value = testValue;
    textarea.dispatchEvent(new Event('input'));
    expect(component.form.get('visaJobIneligiblePathways')?.value).toEqual(testValue);
  });
});
