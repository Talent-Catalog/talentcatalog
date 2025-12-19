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

import {By} from '@angular/platform-browser';
import {DebugElement} from "@angular/core";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {SortedByComponent} from "./sorted-by.component";

describe('SortedByComponent', () => {
  let component: SortedByComponent;
  let fixture: ComponentFixture<SortedByComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SortedByComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SortedByComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display down chevron when sort direction is ASC', () => {
    component.sortColumn = 'name';
    component.sortDirection = 'ASC';
    component.column = 'name';
    fixture.detectChanges();

    const icon: DebugElement = fixture.debugElement.query(By.css('.fa-chevron-down'));
    expect(icon).toBeTruthy();
  });

  it('should display up chevron when sort direction is DESC', () => {
    component.sortColumn = 'name';
    component.sortDirection = 'DESC';
    component.column = 'name';
    fixture.detectChanges();

    const icon: DebugElement = fixture.debugElement.query(By.css('.fa-chevron-up'));
    expect(icon).toBeTruthy();
  });

  it('should not display any icon when column does not match sortColumn', () => {
    component.sortColumn = 'name';
    component.sortDirection = 'ASC';
    component.column = 'date';
    fixture.detectChanges();

    const downIcon: DebugElement = fixture.debugElement.query(By.css('.fa-chevron-down'));
    const upIcon: DebugElement = fixture.debugElement.query(By.css('.fa-chevron-up'));

    expect(downIcon).toBeNull();
    expect(upIcon).toBeNull();
  });
});
