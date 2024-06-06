/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {of} from "rxjs";
import {SimpleChange} from "@angular/core";

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
    component.user = user;
    component.candidate = candidate;
    fixture.detectChanges();
  });

  it('should ', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch user information when the candidate input changes', () => {
    mockUserService.get.and.returnValue(of(user));
    const changes = {
      candidate: new SimpleChange(null, candidate, true)
    };
    component.ngOnChanges(changes);
    expect(mockUserService.get).toHaveBeenCalledWith(1);
    expect(component.loading).toBe(false);
    expect(component.user).toEqual(user);
  });

  it('should open the password update modal when reset link is clicked', () => {
    const modalRef = { componentInstance: { user: null }, result: Promise.resolve(user) };

    mockModalService.open.and.returnValue(modalRef);

    component.updatePassword(user);

    expect(mockModalService.open).toHaveBeenCalledOnceWith(jasmine.any(Function), { centered: true, backdrop: 'static' });
    expect(modalRef.componentInstance.user).toEqual(user);
  });
});
