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
import {CreatedByComponent} from "./created-by.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {UserPipe} from "../user.pipe";
import {DatePipe} from "@angular/common";
import {MockUser} from "../../../../MockData/MockUser";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

describe('CreatedByComponent', () => {
  let component: CreatedByComponent;
  let fixture: ComponentFixture<CreatedByComponent>;
  let userPipe: UserPipe;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreatedByComponent,UserPipe],
      providers:[DatePipe],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreatedByComponent);
    component = fixture.componentInstance;
    userPipe = new UserPipe();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the creator name if createdBy is defined', () => {
    const mockUser = new MockUser();
    component.object = { createdBy: mockUser, createdDate: '2024-07-09T12:00:00' };
    fixture.detectChanges();

    const nameElement = fixture.debugElement.query(By.css('.name')).nativeElement;
    expect(nameElement.textContent).toContain(userPipe.transform(mockUser, 'fullName'));
  });


  it('should display "unknown" if createdBy is not defined', () => {
    component.object = { createdDate: '2024-07-09T12:00:00' };
    fixture.detectChanges();

    const nameElement = fixture.debugElement.query(By.css('.name')).nativeElement;
    expect(nameElement.textContent).toContain('unknown');
  });

  it('should display the formatted creation date if createdDate is defined', () => {
    const datePipe = new DatePipe('en-US'); // Instantiate the DatePipe
    const createdDate = '2024-07-09T12:00:00';
    component.object = { createdBy: 'John Doe', createdDate: createdDate };
    fixture.detectChanges();

    const formattedDate = datePipe.transform(new Date(createdDate), 'customDateTime'); // Adjust the format as needed
    const dateElement = fixture.debugElement.query(By.css('.date')).nativeElement;

    expect(dateElement.textContent).toContain(formattedDate);
  });

  it('should not display the date if createdDate is not defined', () => {
    component.object = { createdBy: 'John Doe' };
    fixture.detectChanges();

    const dateElement = fixture.debugElement.query(By.css('.date'));
    expect(dateElement).toBeNull();
  });
});
