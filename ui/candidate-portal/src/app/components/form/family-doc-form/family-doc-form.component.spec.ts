import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FamilyDocFormComponent} from './family-doc-form.component';

describe('FamilyDocsFormComponent', () => {
  let component: FamilyDocFormComponent;
  let fixture: ComponentFixture<FamilyDocFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [FamilyDocFormComponent]
    });
    fixture = TestBed.createComponent(FamilyDocFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
