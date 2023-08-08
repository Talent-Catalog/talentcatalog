import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OccupationCategoryComponent} from './occupation-category.component';

describe('OccupationCategoryComponent', () => {
  let component: OccupationCategoryComponent;
  let fixture: ComponentFixture<OccupationCategoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OccupationCategoryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OccupationCategoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
