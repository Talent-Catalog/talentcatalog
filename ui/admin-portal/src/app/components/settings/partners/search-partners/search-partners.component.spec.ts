import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchPartnersComponent } from './search-partners.component';

describe('SearchPartnersComponent', () => {
  let component: SearchPartnersComponent;
  let fixture: ComponentFixture<SearchPartnersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearchPartnersComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchPartnersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
