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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {ViewJobSourceContactsComponent} from './view-job-source-contacts.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockPartner} from "../../../../../MockData/MockPartner";

describe('ViewJobSourceContactsComponent', () => {
  let component: ViewJobSourceContactsComponent;
  let fixture: ComponentFixture<ViewJobSourceContactsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ViewJobSourceContactsComponent],
      imports: [HttpClientTestingModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSourceContactsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should emit sourcePartnerSelection event when a source partner is selected', () => {
    const mockPartner: MockPartner = new MockPartner();    component.selectable = true;
    component.sourcePartners = [mockPartner]; // Set up a mock source partner
    spyOn(component.sourcePartnerSelection, 'emit'); // Spy on the emit method of sourcePartnerSelection

    component.selectCurrent(mockPartner); // Call the method to select the mock partner

    expect(component.sourcePartnerSelection.emit).toHaveBeenCalledWith(mockPartner); // Check if emit was called with the mock partner
  });
});
