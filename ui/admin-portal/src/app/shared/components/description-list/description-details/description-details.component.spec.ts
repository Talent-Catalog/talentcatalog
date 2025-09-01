import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DescriptionDetailsComponent } from './description-details.component';

describe('DescriptionDetailsComponent', () => {
  let component: DescriptionDetailsComponent;
  let fixture: ComponentFixture<DescriptionDetailsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DescriptionDetailsComponent]
    });
    fixture = TestBed.createComponent(DescriptionDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
