/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';

import {CreateUpdateChatComponent} from './create-update-chat.component';

describe('CreateUpdateChatComponent', () => {
  let component: CreateUpdateChatComponent;
  let fixture: ComponentFixture<CreateUpdateChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateUpdateChatComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateUpdateChatComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should run ngOnInit without errors', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  it('should render the placeholder message', () => {
    fixture.detectChanges();

    const paragraph = fixture.debugElement.query(By.css('p'));

    expect(paragraph).toBeTruthy();
    expect(paragraph.nativeElement.textContent.trim())
    .toBe('create-update-chat works!');
  });
});
