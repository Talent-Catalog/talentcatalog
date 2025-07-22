import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditMaxEducationLevelComponent } from './edit-max-education-level.component';

describe('EditMaxEducationLevelComponent', () => {
  let component: EditMaxEducationLevelComponent;
  let fixture: ComponentFixture<EditMaxEducationLevelComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [EditMaxEducationLevelComponent]
    });
    fixture = TestBed.createComponent(EditMaxEducationLevelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
