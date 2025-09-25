import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TravelDocFormComponent} from './travel-doc-form.component';

describe('ItalyTravelDocumentFormComponent', () => {
  let component: TravelDocFormComponent;
  let fixture: ComponentFixture<TravelDocFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TravelDocFormComponent]
    });
    fixture = TestBed.createComponent(TravelDocFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
