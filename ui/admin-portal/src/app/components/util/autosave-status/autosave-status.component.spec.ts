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
import {AutosaveStatusComponent} from "./autosave-status.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {By} from '@angular/platform-browser';

describe('AutosaveStatusComponent', () => {
  let component: AutosaveStatusComponent;
  let fixture: ComponentFixture<AutosaveStatusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AutosaveStatusComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AutosaveStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display typing icon when typing is true', () => {
    component.typing = true;
    component.saving = false;
    fixture.detectChanges();

    const typingIcon = fixture.debugElement.query(By.css('.fa-keyboard'));
    const savingIcon = fixture.debugElement.query(By.css('.fa-spinner'));
    const savedIcon = fixture.debugElement.query(By.css('.fa-save'));

    expect(typingIcon).toBeTruthy();
    expect(savingIcon).toBeFalsy();
    expect(savedIcon).toBeFalsy();
  });

  it('should display saving icon when saving is true', () => {
    component.typing = false;
    component.saving = true;
    fixture.detectChanges();

    const typingIcon = fixture.debugElement.query(By.css('.fa-keyboard'));
    const savingIcon = fixture.debugElement.query(By.css('.fa-spinner'));
    const savedIcon = fixture.debugElement.query(By.css('.fa-save'));

    expect(typingIcon).toBeFalsy();
    expect(savingIcon).toBeTruthy();
    expect(savedIcon).toBeFalsy();
  });

  it('should display saved icon when not typing and not saving', () => {
    component.typing = false;
    component.saving = false;
    fixture.detectChanges();

    const typingIcon = fixture.debugElement.query(By.css('.fa-keyboard'));
    const savingIcon = fixture.debugElement.query(By.css('.fa-spinner'));
    const savedIcon = fixture.debugElement.query(By.css('.fa-save'));

    expect(typingIcon).toBeFalsy();
    expect(savingIcon).toBeFalsy();
    expect(savedIcon).toBeTruthy();
  });
});
