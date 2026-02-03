import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RefugeeStatusInfoFormComponent} from './refugee-status-info-form.component';

describe('RefugeeStatusEvidenceFormComponent', () => {
  let component: RefugeeStatusInfoFormComponent;
  let fixture: ComponentFixture<RefugeeStatusInfoFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RefugeeStatusInfoFormComponent]
    });
    fixture = TestBed.createComponent(RefugeeStatusInfoFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
