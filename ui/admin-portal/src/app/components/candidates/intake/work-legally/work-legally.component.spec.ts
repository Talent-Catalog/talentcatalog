// /*
//  * Copyright (c) 2021 Talent Beyond Boundaries.
//  *
//  * This program is free software: you can redistribute it and/or modify it under
//  * the terms of the GNU Affero General Public License as published by the Free
//  * Software Foundation, either version 3 of the License, or any later version.
//  *
//  * This program is distributed in the hope that it will be useful, but WITHOUT
//  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
//  * for more details.
//  *
//  * You should have received a copy of the GNU Affero General Public License
//  * along with this program. If not, see https://www.gnu.org/licenses/.
//  */
// import {WorkLegallyComponent} from "./work-legally.component";
// import {ComponentFixture, TestBed} from "@angular/core/testing";
// import {ReactiveFormsModule} from "@angular/forms";
// import {NgSelectModule} from "@ng-select/ng-select";
// import {CandidateService} from "../../../../services/candidate.service";
// import {HttpClientTestingModule} from "@angular/common/http/testing";
// import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
//
// describe('WorkLegallyComponent', () => {
//   let component: WorkLegallyComponent;
//   let fixture: ComponentFixture<WorkLegallyComponent>;
//
//   beforeEach(async () => {
//     await TestBed.configureTestingModule({
//       declarations: [ WorkLegallyComponent, AutosaveStatusComponent ],
//       imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
//       providers: [
//         { provide: CandidateService }
//       ]
//     })
//     .compileComponents();
//   });
//
//   beforeEach(() => {
//     fixture = TestBed.createComponent(WorkLegallyComponent);
//     component = fixture.componentInstance;
//     // component.candidateIntakeData = {
//     //   workLegally:
//     // };
//     fixture.detectChanges();
//   });
//
//   it('should ', () => {
//     expect(component).toBeTruthy();
//   });
//
//   // it('should initialize the form control with the correct default value', () => {
//   //   expect(component.form.get('workLegally').value).toBe('Yes');
//   // });
//   //
//   // it('should update the form control when candidateIntakeData changes', () => {
//   //   component.candidateIntakeData = {
//   //     workLegally: 'No'
//   //   };
//   //   component.ngOnInit();
//   //   fixture.detectChanges();
//   //   expect(component.form.get('workLegally').value).toBe('No');
//   // });
//
//   // it('should initialize the form control correctly when candidateIntakeData is null', () => {
//   //   component.candidateIntakeData = null;
//   //   component.ngOnInit();
//   //   fixture.detectChanges();
//   //   expect(component.form.get('workLegally').value).toBeNull();
//   // });
// });
