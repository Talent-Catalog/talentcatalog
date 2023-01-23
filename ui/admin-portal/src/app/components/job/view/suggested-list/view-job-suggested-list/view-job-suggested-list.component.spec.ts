import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewJobSuggestedListComponent} from './view-job-suggested-list.component';

describe('ViewJobSuggestedListComponent', () => {
  let component: ViewJobSuggestedListComponent;
  let fixture: ComponentFixture<ViewJobSuggestedListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewJobSuggestedListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSuggestedListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
