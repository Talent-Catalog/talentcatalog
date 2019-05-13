import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchCandidatesComponent } from './search-candidates.component';

describe('SearchCandidatesComponent', () => {
  let component: SearchCandidatesComponent;
  let fixture: ComponentFixture<SearchCandidatesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchCandidatesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchCandidatesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
