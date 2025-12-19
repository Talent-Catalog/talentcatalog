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
import {CandidateCitizenshipCardComponent} from "./candidate-citizenship-card.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {CandidateService} from "../../../../../services/candidate.service";
import {CandidateCitizenshipService} from "../../../../../services/candidate-citizenship.service";
import {Country} from "../../../../../model/country";
import {CandidateIntakeData, HasPassport} from "../../../../../model/candidate";
import {of} from "rxjs";

describe('CandidateCitizenshipCardComponent', () => {
  let component: CandidateCitizenshipCardComponent;
  let fixture: ComponentFixture<CandidateCitizenshipCardComponent>;
  let mockCitizenshipService: jasmine.SpyObj<CandidateCitizenshipService>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateCitizenshipCardComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService},
        { provide: CandidateCitizenshipService, useValue: jasmine.createSpyObj('CandidateCitizenshipService', ['delete']) }
      ]
    })
    .compileComponents();

    mockCitizenshipService = TestBed.inject(CandidateCitizenshipService) as jasmine.SpyObj<CandidateCitizenshipService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateCitizenshipCardComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      candidateCitizenships: [
        { id: 1, nationality: { name: 'Afghanistan' } as Country, hasPassport: HasPassport.ValidPassport, passportExp: '2025-12-31', notes: 'Valid passport' },
        { id: 2, nationality: { name: 'Iran' } as Country, hasPassport: HasPassport.InvalidPassport, notes: 'No passport' }
      ]
    } as CandidateIntakeData;
    component.myRecordIndex = 0; // Assuming the component is initialized with the first record
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should delete citizenship record and trigger deletion event', () => {
    const deleteResponse = of(null);
    mockCitizenshipService.delete.and.returnValue(deleteResponse);

    spyOn(component.delete, 'emit');

    component.doDelete();

    expect(mockCitizenshipService.delete).toHaveBeenCalledWith(1); // Assuming the first record is being deleted
    expect(component.delete.emit).toHaveBeenCalled();
    expect(component.error).toBeFalsy();
  });
});
