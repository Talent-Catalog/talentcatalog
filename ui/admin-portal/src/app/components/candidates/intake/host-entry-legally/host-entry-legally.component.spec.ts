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
import {HostEntryLegallyComponent} from "./host-entry-legally.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {CandidateService} from "../../../../services/candidate.service";
import {EnumOption} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";

describe('HostEntryLegallyComponent', () => {
  let component: HostEntryLegallyComponent;
  let fixture: ComponentFixture<HostEntryLegallyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      declarations: [HostEntryLegallyComponent,AutosaveStatusComponent],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService } // Provide the mock service
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HostEntryLegallyComponent);
    component = fixture.componentInstance;

    component.candidateIntakeData = {
      hostEntryLegally: YesNo.Yes
    };
    // Manually trigger ngOnInit
    fixture.detectChanges(); // Trigger initial data binding
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with candidate data', () => {
    const expectedLegallyOption: EnumOption[] = [
      { key: 'Yes', stringValue: 'Yes' },
      { key: 'No', stringValue: 'No' },
      { key: 'NoResponse', stringValue: 'NoResponse' },
    ];
    const expectedEnterLegally = YesNo.Yes; // Assuming default value for testing

    expect(component.form.get('hostEntryLegally').value).toBe(expectedEnterLegally);
    expect(component.hostEntryLegallyOptions).toEqual(expectedLegallyOption);
  });

  it('should display the notes textarea when "No" is selected', () => {
    component.form.get('hostEntryLegally').setValue('No');
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const notesTextarea = compiled.querySelector('textarea');
    expect(notesTextarea).toBeTruthy();
  });

  it('should not display the notes textarea when "Yes" is selected', () => {
    component.form.get('hostEntryLegally').setValue('Yes');
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const notesTextarea = compiled.querySelector('textarea');
    expect(notesTextarea).toBeNull();
  });
});
