import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PotentialDuplicateIconComponent } from './potential-duplicate-icon.component';

describe('PotentialDuplicateIconComponent', () => {
  let component: PotentialDuplicateIconComponent;
  let fixture: ComponentFixture<PotentialDuplicateIconComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PotentialDuplicateIconComponent]
    });
    fixture = TestBed.createComponent(PotentialDuplicateIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
