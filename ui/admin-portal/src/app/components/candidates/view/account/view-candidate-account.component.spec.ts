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
import {ViewCandidateAccountComponent} from "./view-candidate-account.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {UserService} from "../../../../services/user.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {MockUser} from "../../../../MockData/MockUser";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterLinkStubDirective} from "../../../login/login.component.spec";
import {Candidate} from "../../../../model/candidate";
import {User} from "../../../../model/user";

fdescribe('ViewCandidateAccountComponent', () => {
  let component: ViewCandidateAccountComponent;
  let fixture: ComponentFixture<ViewCandidateAccountComponent>;
  let mockUserService;
  let mockModalService;

  const candidate: Candidate = new MockCandidate();
  const user: User = new MockUser();
  beforeEach(async () => {
    mockUserService = jasmine.createSpyObj('UserService', ['get']);
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateAccountComponent,RouterLinkStubDirective ],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: NgbModal, useValue: mockModalService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateAccountComponent);
    component = fixture.componentInstance;
    component.user = candidate.user;
    component.candidate = candidate;
    fixture.detectChanges();
  });

  it('should ', () => {
    expect(component).toBeTruthy();
  });

  it('should open the password update modal when reset link is clicked', () => {
    const modalRef = { componentInstance: { user: null }, result: Promise.resolve(user) };

    mockModalService.open.and.returnValue(modalRef);

    component.updatePassword(user);

    expect(mockModalService.open).toHaveBeenCalledOnceWith(jasmine.any(Function), { centered: true, backdrop: 'static' });
    expect(modalRef.componentInstance.user).toEqual(user);
  });

  it('should hide password reset link if editable is false', () => {
    component.editable = false;
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const resetLink = compiled.querySelector('a.small');
    expect(resetLink).toBeFalsy();
  });
});
