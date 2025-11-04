import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RsdEvidenceFormComponent } from './rsd-evidence-form.component';

describe('RsdEvidenceFormComponent', () => {
  let component: RsdEvidenceFormComponent;
  let fixture: ComponentFixture<RsdEvidenceFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RsdEvidenceFormComponent]
    });
    fixture = TestBed.createComponent(RsdEvidenceFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
