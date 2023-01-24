import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewJobPreparationItemsComponent} from './view-job-preparation-items.component';

describe('ViewJobPreparationItemsComponent', () => {
  let component: ViewJobPreparationItemsComponent;
  let fixture: ComponentFixture<ViewJobPreparationItemsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewJobPreparationItemsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobPreparationItemsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
