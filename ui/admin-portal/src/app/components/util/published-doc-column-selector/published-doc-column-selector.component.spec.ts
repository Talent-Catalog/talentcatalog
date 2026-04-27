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

import {HttpClientTestingModule} from "@angular/common/http/testing";
import {By} from "@angular/platform-browser";
import {DatePipe, TitleCasePipe} from "@angular/common";
import {
  mockPublishedDocColumnConfig,
  mockPublishedDocColumnConfigs
} from "../../../MockData/MockPublishDoc";
import {PublishedDocColumnSelectorComponent} from "./published-doc-column-selector.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {FormsModule} from "@angular/forms";
import {NgbActiveModal, NgbDropdownConfig, NgbDropdownModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {DragulaModule, DragulaService} from "ng2-dragula";
import {PublishedDocColumnService} from "../../../services/published-doc-column.service";
import {CandidateSourceService} from "../../../services/candidate-source.service";
describe('PublishedDocColumnSelectorComponent', () => {
  let component: PublishedDocColumnSelectorComponent;
  let fixture: ComponentFixture<PublishedDocColumnSelectorComponent>;
  let publishedDocColumnServiceSpy: jasmine.SpyObj<PublishedDocColumnService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  beforeEach(async () => {
    const publishedDocColumnService = jasmine.createSpyObj('PublishedDocColumnService', ['getDefaultColumns']);
    const activeModal = jasmine.createSpyObj('NgbActiveModal', ['dismiss', 'close']);
    await TestBed.configureTestingModule({

      declarations: [ PublishedDocColumnSelectorComponent],
      imports: [
        HttpClientTestingModule,
        FormsModule,
        NgbDropdownModule,
        NgSelectModule,
        DragulaModule.forRoot()
      ],
      providers: [
        { provide: PublishedDocColumnService, useValue: publishedDocColumnService },
        DatePipe,
        TitleCasePipe,
        CandidateSourceService,
        { provide: NgbActiveModal, useValue: activeModal },
        NgbDropdownConfig,
        DragulaService
      ]
    })
    .compileComponents();
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    publishedDocColumnServiceSpy = TestBed.inject(PublishedDocColumnService) as jasmine.SpyObj<PublishedDocColumnService>;
  });
  beforeEach(() => {
    fixture = TestBed.createComponent(PublishedDocColumnSelectorComponent);
    component = fixture.componentInstance;
    component.selectedColumns = mockPublishedDocColumnConfigs;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display error message when error exists', () => {
    component.selectedColumns = mockPublishedDocColumnConfigs;
    component.error = 'Error message';
    fixture.detectChanges();
    const alertElement = fixture.debugElement.query(By.css('.alert-danger'));
    expect(alertElement).toBeTruthy();
    expect(alertElement.nativeElement.textContent).toContain('Error message');
  });

  it('should remove column when removeColumn is called', () => {
    component.selectedColumns = [mockPublishedDocColumnConfig];
    component.removeColumn(mockPublishedDocColumnConfig);
    expect(component.selectedColumns.length).toBe(0);
  });

  it('should add column when addColumn is called', () => {
    component.addColumn(mockPublishedDocColumnConfig);
    expect(component.selectedColumns.length).toBe(3);
    expect(component.selectedColumns[2].columnDef.key).toBe('mockKey');
  });

  it('should update field when update is called', () => {
    const field = mockPublishedDocColumnConfig;
    expect(component.update(field)).toEqual(field);
  });

  it('should call activeModal.close(selectedColumns) when submit is called', () => {
    component.selectedColumns = mockPublishedDocColumnConfigs;
    component.submit();
    expect(activeModalSpy.close).toHaveBeenCalledWith(component.selectedColumns);
  });

  it('should call activeModal.dismiss(false) when cancel is called', () => {
    component.cancel();
    expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
  });


});
