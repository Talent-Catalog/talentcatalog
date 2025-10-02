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
import {ViewJobInfoComponent} from './view-job-info.component';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {EditJobInfoComponent} from "../edit-job-info/edit-job-info.component";
import {MockJob} from "../../../../../MockData/MockJob";
import {RouterLinkStubDirective} from "../../../../login/login.component.spec";
import {AuthorizationService} from "../../../../../services/authorization.service";

describe('ViewJobInfoComponent', () => {
  let component: ViewJobInfoComponent;
  let fixture: ComponentFixture<ViewJobInfoComponent>;
  let modalService: NgbModal;
   beforeEach(async () => {
     let authServiceSpy =
       jasmine.createSpyObj('AuthorizationService', ['canSeeJobDetails']);
     authServiceSpy.canSeeJobDetails.and.returnValue(true);

     await TestBed.configureTestingModule({
      declarations: [ ViewJobInfoComponent,RouterLinkStubDirective ],
      providers: [
        { provide: NgbModal  },
        { provide: AuthorizationService, useValue: authServiceSpy  },
      ]
    })
    .compileComponents();

    modalService = TestBed.inject(NgbModal);
  });

  beforeEach(() => { // Use this block for component creation
    fixture = TestBed.createComponent(ViewJobInfoComponent);
    component = fixture.componentInstance;

    component.editable = true;
    // Provide a mock Job object
    component.job = MockJob;

    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should display job information correctly', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('#country').textContent).toContain('USA');
   });

  it('should open edit modal when edit button is clicked', () => {
    // Spy on modalService.open method and return a dummy NgbModalRef
    spyOn(modalService, 'open').and.returnValue({
      componentInstance: {},
      result: Promise.resolve('saved')
    } as NgbModalRef);

    // Trigger the editJobInfo method, for example, by clicking the edit button
    const editButton = fixture.nativeElement.querySelector('.btn-secondary');
    editButton.click();

    // Expect that modalService.open was called with EditJobInfoComponent
    expect(modalService.open).toHaveBeenCalledWith(EditJobInfoComponent, jasmine.any(Object));
  });

});
