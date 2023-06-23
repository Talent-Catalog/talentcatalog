import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FilteredOppsComponent} from './filtered-opps.component';

describe('FilteredOppsComponent', () => {
  let component: FilteredOppsComponent;
  let fixture: ComponentFixture<FilteredOppsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FilteredOppsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FilteredOppsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
