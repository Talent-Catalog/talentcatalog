import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OfferToAssistComponent} from './offer-to-assist.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {OfferToAssistService} from "../../../services/offer-to-assist.service";
import {SearchResults} from "../../../model/search-results";
import {CandidateAssistanceType, OfferToAssist} from "../../../model/offer-to-assist";
import {MockPartner} from "../../../MockData/MockPartner";
import {of} from "rxjs";

describe('OfferToAssistComponent', () => {
  let component: OfferToAssistComponent;
  let offerToAssistServiceSpy: jasmine.SpyObj<OfferToAssistService>;
  let fixture: ComponentFixture<OfferToAssistComponent>;
  const mockResults: SearchResults<OfferToAssist> = {
    first: false,
    last: false,
    number: 0,
    size: 0,
    totalPages: 0,
    totalElements: 1,
    content: [
      {id: 1,
      createdDate: new Date("2024-05-01"),
      additionalNotes: 'notes',
      partner: new MockPartner(),
      publicId: '1234abcd',
      reason: CandidateAssistanceType.JOB_OPPORTUNITY}]
  };

  beforeEach(() => {
    const offerToAssistSpy = jasmine.createSpyObj('OfferToAssistService', ['search']);
    TestBed.configureTestingModule({
      declarations: [OfferToAssistComponent],
      imports: [FormsModule, ReactiveFormsModule],
      providers: [
        { provide: OfferToAssistService, useValue: offerToAssistSpy },
      ]
    });
    fixture = TestBed.createComponent(OfferToAssistComponent);
    component = fixture.componentInstance;
    offerToAssistServiceSpy = TestBed.inject(OfferToAssistService) as jasmine.SpyObj<OfferToAssistService>;
    offerToAssistServiceSpy.search.and.returnValue(of(mockResults));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize forms and set default values', () => {
    component.ngOnInit();
    expect(component.searchForm).toBeDefined();
    expect(component.pageNumber).toBe(1);
    expect(component.pageSize).toBe(50);
  });

  it('should search offers to assist', () => {

    component.search();
    expect(offerToAssistServiceSpy.search).toHaveBeenCalled();
    expect(component.results).toEqual(mockResults);
    expect(component.loading).toBeFalse();
  });
});
