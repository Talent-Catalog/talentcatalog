import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TravelDocumentComponent} from './travel-document.component';

describe('TravelDocumentComponent', () => {
  let component: TravelDocumentComponent;
  let fixture: ComponentFixture<TravelDocumentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TravelDocumentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TravelDocumentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
