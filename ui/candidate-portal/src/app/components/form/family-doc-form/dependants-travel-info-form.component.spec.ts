import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DependantsTravelInfoFormComponent} from './dependants-travel-info-form.component';

describe('FamilyDocsFormComponent', () => {
  let component: DependantsTravelInfoFormComponent;
  let fixture: ComponentFixture<DependantsTravelInfoFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DependantsTravelInfoFormComponent]
    });
    fixture = TestBed.createComponent(DependantsTravelInfoFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
