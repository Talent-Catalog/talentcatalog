import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FamilyRsdEvidenceFormComponent } from './family-rsd-evidence-form.component';

describe('FamilyRsdEvidenceFormComponent', () => {
  let component: FamilyRsdEvidenceFormComponent;
  let fixture: ComponentFixture<FamilyRsdEvidenceFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [FamilyRsdEvidenceFormComponent]
    });
    fixture = TestBed.createComponent(FamilyRsdEvidenceFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
