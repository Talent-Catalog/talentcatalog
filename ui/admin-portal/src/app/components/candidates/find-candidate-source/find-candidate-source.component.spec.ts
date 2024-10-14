import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FindCandidateSourceComponent} from './find-candidate-source.component';
import {NgbTypeaheadSelectItemEvent} from "@ng-bootstrap/ng-bootstrap";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {of} from "rxjs";
import {MockCandidateSource} from "../../../MockData/MockCandidateSource";

describe('FindListComponent', () => {
  let component: FindCandidateSourceComponent;
  let fixture: ComponentFixture<FindCandidateSourceComponent>;
  let candidateSourceService: jasmine.SpyObj<CandidateSourceService>;
  let mockCandidateSource: MockCandidateSource = new MockCandidateSource();

  beforeEach(async () => {
    const candidateSourceServiceSpy = jasmine.createSpyObj('CandidateSourceService', ['searchPaged', 'get']);

    await TestBed.configureTestingModule({
      declarations: [ FindCandidateSourceComponent ],
      providers: [
        { provide: CandidateSourceService, useValue: candidateSourceServiceSpy }
      ]

    })
    .compileComponents();

    candidateSourceService = TestBed.inject(CandidateSourceService) as jasmine.SpyObj<CandidateSourceService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FindCandidateSourceComponent);
    component = fixture.componentInstance;
    candidateSourceService.get.and.returnValue(of(mockCandidateSource));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should initialize correctly', () => {
    component.id = 1;
    component.ngOnChanges({});

    expect(component.currentSelection.length).toEqual(1);
    expect(component.currentSelection[0]).toEqual(mockCandidateSource);
  });

  it('should emit job selection correctly', (done) => {
    component.selectionMade.subscribe(selectedSources => {
      expect(selectedSources.length).toEqual(1);
      expect(selectedSources[0]).toEqual(mockCandidateSource);
      done();
    });

    component.selectResult({ item: mockCandidateSource } as NgbTypeaheadSelectItemEvent);
  });
});
