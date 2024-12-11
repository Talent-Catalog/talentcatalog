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
import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {EditJobInfoComponent} from "../edit-job-info/edit-job-info.component";
import {UntypedFormBuilder} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockJob} from "../../../../../MockData/MockJob";
import {RouterLinkStubDirective} from "../../../../login/login.component.spec";
describe('EditJobInfoComponent', () => {
  let component: EditJobInfoComponent;
  let fixture: ComponentFixture<EditJobInfoComponent>;
  let modalService: NgbModal;
  let ngbActiveModal: NgbActiveModal;
  let fb: UntypedFormBuilder;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditJobInfoComponent,RouterLinkStubDirective ],
      imports:[HttpClientTestingModule],
      providers: [
        { provide: NgbModal  },
        { provide: NgbActiveModal  },
        { provide: UntypedFormBuilder  },
      ]
    })
    .compileComponents();

    modalService = TestBed.inject(NgbModal);
    ngbActiveModal = TestBed.inject(NgbActiveModal);
    fb = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => { // Use this block for component creation
    fixture = TestBed.createComponent(EditJobInfoComponent);
    component = fixture.componentInstance;

     // Provide a mock Job object
    component.job = MockJob;

    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize jobForm with correct values', () => {
    component.jobForm = new UntypedFormBuilder().group(MockJob);
    // Access the form controls
    const submissionDueDateControl = component.jobForm.get('submissionDueDate');
    const contactUserControl = component.jobForm.get('contactUser');
    expect(submissionDueDateControl.value.toDateString()).toEqual(component.job.submissionDueDate.toDateString());
    expect(contactUserControl.value).toEqual(component.job.contactUser);

   });
});
