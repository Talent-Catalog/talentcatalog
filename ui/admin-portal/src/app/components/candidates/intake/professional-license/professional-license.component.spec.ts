import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ProfessionalLicenseComponent} from './professional-license.component';

describe('ProfessionalLicenseComponent', () => {
  let component: ProfessionalLicenseComponent;
  let fixture: ComponentFixture<ProfessionalLicenseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProfessionalLicenseComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfessionalLicenseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
