import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {IntProtectionComponent} from './int-protection.component';

describe('IntProtectionComponent', () => {
  let component: IntProtectionComponent;
  let fixture: ComponentFixture<IntProtectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IntProtectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntProtectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
