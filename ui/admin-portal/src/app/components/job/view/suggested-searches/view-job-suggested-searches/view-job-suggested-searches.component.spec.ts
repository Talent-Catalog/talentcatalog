import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewJobSuggestedSearchesComponent} from './view-job-suggested-searches.component';

describe('ViewJobSuggestedSearchesComponent', () => {
  let component: ViewJobSuggestedSearchesComponent;
  let fixture: ComponentFixture<ViewJobSuggestedSearchesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewJobSuggestedSearchesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSuggestedSearchesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
