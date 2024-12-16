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

import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {ShowCandidatesWithChatComponent} from './show-candidates-with-chat.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {NgbPagination} from "@ng-bootstrap/ng-bootstrap";
import {By} from "@angular/platform-browser";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {RouterLinkStubDirective} from "../../login/login.component.spec";
import {CandidateService} from "../../../services/candidate.service";
import {SearchResults} from "../../../model/search-results";
import {of} from "rxjs";

describe('ShowCandidatesWithChatComponent', () => {
  let component: ShowCandidatesWithChatComponent;
  let fixture: ComponentFixture<ShowCandidatesWithChatComponent>;
  let formBuilder: UntypedFormBuilder;
  let candidateService: jasmine.SpyObj<CandidateService>;
  const mockCandidate = new MockCandidate();
  const mockSearchResults = new SearchResults<MockCandidate>();
  const mockCandidates = [mockCandidate, mockCandidate]
  mockSearchResults.content = mockCandidates;

  candidateService = jasmine.createSpyObj(
    'CandidateService', ['fetchCandidatesWithChat', 'checkUnreadChats']
  )
  candidateService.fetchCandidatesWithChat.and.returnValue(of(mockSearchResults));
  // Not testing this one at present, so we just skip and avoid having to resolve downstream errors
  candidateService.checkUnreadChats.and.returnValue(of(null));

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ShowCandidatesWithChatComponent, RouterLinkStubDirective, SortedByComponent ],
      imports: [ HttpClientTestingModule, FormsModule, ReactiveFormsModule, NgbPagination ],
      providers: [
        { provide: UntypedFormBuilder },
        { provide: CandidateService, useValue: candidateService }
      ]
    })
    .compileComponents();

    formBuilder = TestBed.inject(UntypedFormBuilder); // Inject FormBuilder
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShowCandidatesWithChatComponent);
    component = fixture.componentInstance;

    // Initialize form group
    component.searchForm = formBuilder.group({
      keyword: [''], // Initialize with default value
      unreadOnly: [false]
      // Add more form controls here
    });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values and configurations', () => {
    // Check if component initializes with default values
    expect(component.pageNumber).toBe(1);
    expect(component.pageSize).toBe(25);
    expect(component.sortField).toBe('id');
    expect(component.sortDirection).toBe('DESC');
    expect(component.chatsReadProcessingComplete).toBeFalse();
    expect(component.currentCandidate).toBe(null);
    expect(component.candidates).toEqual(mockCandidates);
    expect(component.results).toEqual(mockSearchResults);
  });

  it('should initialize the form correctly', () => {
    const formControls = component.searchForm.controls;
    expect(component.searchForm).toBeDefined(); // Check that form is defined
    // Check that form controls are initialized
    expect(formControls.keyword.value).toBe('');
    expect(formControls.unreadOnly.value).toBe(false);
  });

  it('should fetch candidates again when search term entered', fakeAsync(() => {
    spyOn(component, 'fetchCandidatesWithActiveChat');
    // Simulate user input of search term
    const inputElement =
      fixture.debugElement.query(By.css('input[formControlName="keyword"]')).nativeElement;
    inputElement.value = 'test keyword';
    inputElement.dispatchEvent(new Event('input'));

    fixture.detectChanges();
    tick(1000) // Replicate the filter subscription's debounce time

    // Check that the form control's value has been updated
    expect(component.searchForm.get('keyword').value).toBe('test keyword');

    expect(component.fetchCandidatesWithActiveChat).toHaveBeenCalledWith(true);
  }))

  it('should fetch candidates again when unreadOnly checkbox is checked',
    fakeAsync(() => {
    spyOn(component, 'fetchCandidatesWithActiveChat');
    // Simulate user checking box
    const checkboxElement =
      fixture.debugElement.query(By.css('input[formControlName="unreadOnly"]')).nativeElement;
    checkboxElement.checked = true;
    checkboxElement.dispatchEvent(new Event('change'));

    fixture.detectChanges();
    tick(1000) // Replicate the filter subscription's debounce time

    // Check that the form control's value has been updated
    expect(component.searchForm.get('unreadOnly').value).toBeTrue();

    expect(component.fetchCandidatesWithActiveChat).toHaveBeenCalledWith(true);
  }))

  it('should fetch candidates again when Refresh button is clicked',() => {
      spyOn(component, 'fetchCandidatesWithActiveChat');
      spyOn(component, 'refresh');

      // Simulate user clicking button
      const buttonElement =
        fixture.debugElement.query(By.css('button.btn-accent-3')).nativeElement;
      buttonElement.click();

      expect(component.refresh).toHaveBeenCalled();

      expect(component.fetchCandidatesWithActiveChat).toHaveBeenCalledWith(true);
    })

  it('should emit the candidateSelection method when a candidate row is clicked', () => {
    spyOn(component, 'onCandidateSelected');

    const rowElement = fixture.debugElement.queryAll(By.css('tr'))[1].nativeElement;
    rowElement.click();

    // Assert that the onRowClick method was called
    expect(component.onCandidateSelected).toHaveBeenCalledWith(mockCandidate);
  })

  it('should call toggleSort when a sortable column\'s header is clicked',
    () => {
    spyOn(component, 'fetchCandidatesWithActiveChat');
    spyOn(component, 'toggleSort');

    // Click a random sortable header
    const headerElements = fixture.debugElement.queryAll(By.css('th'));
    // -2 because we don't want to click the unread column (last in the array), which isn't sortable
    const randomIndex = Math.floor(Math.random() * (headerElements.length - 2));
    headerElements[randomIndex].nativeElement.click();

    expect(component.toggleSort).toHaveBeenCalled();
  })

  it('should emit the candidateSelection method passing null ' +
    'and set currentCandidate to null when candidates are fetched again', () => {
    spyOn(component.candidateSelection, 'emit');
    spyOn<any>(component, 'processSearchResults').and.callFake(() => {});

    component.fetchCandidatesWithActiveChat(true);

    expect(component.candidateSelection.emit).toHaveBeenCalledWith(null);
    expect(component.currentCandidate).toBe(null);
  })

  it('should check unread chats when candidates are fetched', () => {
    component.fetchCandidatesWithActiveChat(true);

    expect(candidateService.checkUnreadChats).toHaveBeenCalled()
  })

});
