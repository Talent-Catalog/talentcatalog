import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SearchExternalLinksComponent} from './search-external-links.component';

describe('SearchExternalLinksComponent', () => {
  let component: SearchExternalLinksComponent;
  let fixture: ComponentFixture<SearchExternalLinksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearchExternalLinksComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchExternalLinksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
