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
import {By} from '@angular/platform-browser';
import {JobSourceContactsTabComponent} from './job-source-contacts-tab.component';
import {MockJob} from "../../../../../MockData/MockJob";
import {
  JobSourceContactsWithChatsComponent
} from "../../source-contacts/job-source-contacts-with-chats/job-source-contacts-with-chats.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {
  ViewJobSourceContactsComponent
} from "../../source-contacts/view-job-source-contacts/view-job-source-contacts.component";

describe('JobSourceContactsTabComponent', () => {
  let component: JobSourceContactsTabComponent;
  let fixture: ComponentFixture<JobSourceContactsTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports:[HttpClientTestingModule],
      declarations: [JobSourceContactsTabComponent,JobSourceContactsWithChatsComponent,ViewJobSourceContactsComponent],
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobSourceContactsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render job source contacts with chats', () => {
    component.job = MockJob;

    fixture.detectChanges();

    const viewJobSourceContactsComponent = fixture.debugElement.query(By.directive(ViewJobSourceContactsComponent)).componentInstance;
    expect(viewJobSourceContactsComponent).toBeTruthy();

    // Now you can access the job property of the app-view-job-source-contacts component
    const jobFromComponent = viewJobSourceContactsComponent.job;
    expect(jobFromComponent).toEqual(component.job);
  });

});
