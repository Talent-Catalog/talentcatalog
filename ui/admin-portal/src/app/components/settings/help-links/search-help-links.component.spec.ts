import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SearchHelpLinksComponent} from './search-help-links.component';

describe('HelpLinksComponent', () => {
  let component: SearchHelpLinksComponent;
  let fixture: ComponentFixture<SearchHelpLinksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearchHelpLinksComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchHelpLinksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
