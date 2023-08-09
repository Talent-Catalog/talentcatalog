import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AgeRequirementComponent} from './age-requirement.component';

describe('AgeRequirementComponent', () => {
  let component: AgeRequirementComponent;
  let fixture: ComponentFixture<AgeRequirementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AgeRequirementComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AgeRequirementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
