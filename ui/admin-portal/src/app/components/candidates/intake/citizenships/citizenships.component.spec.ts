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
import {CitizenshipsComponent} from "./citizenships.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {CandidateCitizenshipService} from "../../../../services/candidate-citizenship.service";
import {CandidateIntakeData, HasPassport} from "../../../../model/candidate";
import {Country} from "../../../../model/country";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CandidateCitizenshipCardComponent} from "./card/candidate-citizenship-card.component";

describe('CitizenshipsComponent', () => {
  let component: CitizenshipsComponent;
  let fixture: ComponentFixture<CitizenshipsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CitizenshipsComponent, AutosaveStatusComponent, CandidateCitizenshipCardComponent],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        { provide: CandidateCitizenshipService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CitizenshipsComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      candidateCitizenships: [
        { id: 1, nationality: { name: 'Afghanistan' } as Country, hasPassport: HasPassport.ValidPassport, passportExp: '2025-12-31', notes: 'Valid passport' },
        { id: 2, nationality: { name: 'Iran' } as Country, hasPassport: HasPassport.InvalidPassport, notes: 'No passport' }
      ]
    } as CandidateIntakeData;
    fixture.detectChanges(); // Trigger initial data binding
  });

  it('should delete a citizenship record from the list', () => {
    expect(component.candidateIntakeData.candidateCitizenships.length).toBe(2);

    // Simulate deletion of the first citizenship
    component.deleteRecord(0);
    fixture.detectChanges();

    expect(component.candidateIntakeData.candidateCitizenships.length).toBe(1);
    expect(component.candidateIntakeData.candidateCitizenships[0].id).toBe(2);
  });
});
