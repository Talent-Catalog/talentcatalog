import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TravelInfoFormComponent} from './travel-info-form.component';

describe('ItalyTravelDocumentFormComponent', () => {
  let component: TravelInfoFormComponent;
  let fixture: ComponentFixture<TravelInfoFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TravelInfoFormComponent]
    });
    fixture = TestBed.createComponent(TravelInfoFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
