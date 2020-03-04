import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SavedSearchResultsComponent } from './saved-search-results.component';

describe('ReturnsComponent', () => {
  let component: SavedSearchResultsComponent;
  let fixture: ComponentFixture<SavedSearchResultsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SavedSearchResultsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SavedSearchResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
