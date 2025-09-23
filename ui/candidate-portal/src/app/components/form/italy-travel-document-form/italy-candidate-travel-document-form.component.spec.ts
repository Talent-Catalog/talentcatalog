import {ComponentFixture, TestBed} from '@angular/core/testing';

import {
  ItalyCandidateTravelDocumentFormComponent
} from './italy-candidate-travel-document-form.component';

describe('ItalyTravelDocumentFormComponent', () => {
  let component: ItalyCandidateTravelDocumentFormComponent;
  let fixture: ComponentFixture<ItalyCandidateTravelDocumentFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ItalyCandidateTravelDocumentFormComponent]
    });
    fixture = TestBed.createComponent(ItalyCandidateTravelDocumentFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
