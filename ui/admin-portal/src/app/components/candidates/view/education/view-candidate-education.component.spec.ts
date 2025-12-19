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

// describe('ViewCandidateEducationComponent', () => {
//   let component: ViewCandidateEducationComponent;
//   let fixture: ComponentFixture<ViewCandidateEducationComponent>;
//   let mockCandidateEducationService: jasmine.SpyObj<CandidateEducationService>;
//   let mockModalService: jasmine.SpyObj<NgbModal>;
//   const mockCandidate = new MockCandidate();
//   beforeEach(async () => {
//     const candidateEducationServiceSpy = jasmine.createSpyObj('CandidateEducationService', ['list']);
//     const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);
//
//     await TestBed.configureTestingModule({
//       declarations: [ViewCandidateEducationComponent],
//       imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
//       providers: [
//         { provide: CandidateEducationService, useValue: candidateEducationServiceSpy },
//         { provide: NgbModal, useValue: modalServiceSpy }
//       ]
//     })
//     .compileComponents();
//
//     mockCandidateEducationService = TestBed.inject(CandidateEducationService) as jasmine.SpyObj<CandidateEducationService>;
//     mockModalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
//   });
//
//   beforeEach(() => {
//     fixture = TestBed.createComponent(ViewCandidateEducationComponent);
//     component = fixture.componentInstance;
//     component.candidate = mockCandidate;
//     fixture.detectChanges();
//   });
// });
