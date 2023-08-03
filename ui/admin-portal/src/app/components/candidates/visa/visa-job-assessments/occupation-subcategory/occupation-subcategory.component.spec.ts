import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OccupationSubcategoryComponent} from './occupation-subcategory.component';

describe('OccupationSubcategoryComponent', () => {
  let component: OccupationSubcategoryComponent;
  let fixture: ComponentFixture<OccupationSubcategoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OccupationSubcategoryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OccupationSubcategoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
