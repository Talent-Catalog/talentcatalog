import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OfferToAssistComponent} from './offer-to-assist.component';

describe('OfferToAssistComponent', () => {
  let component: OfferToAssistComponent;
  let fixture: ComponentFixture<OfferToAssistComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [OfferToAssistComponent]
    });
    fixture = TestBed.createComponent(OfferToAssistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
