import {ComponentFixture, TestBed} from '@angular/core/testing';

import {
  DependantsRefugeeStatusInfoFormComponent
} from './dependants-refugee-status-info-form.component';

describe('DependantsRefugeeStatusInfoFormComponent', () => {
  let component: DependantsRefugeeStatusInfoFormComponent;
  let fixture: ComponentFixture<DependantsRefugeeStatusInfoFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DependantsRefugeeStatusInfoFormComponent]
    });
    fixture = TestBed.createComponent(DependantsRefugeeStatusInfoFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
