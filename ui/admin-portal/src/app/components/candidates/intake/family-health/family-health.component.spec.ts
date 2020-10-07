import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {FamilyHealthComponent} from './family-health.component';

describe('FamilyHealthComponent', () => {
  let component: FamilyHealthComponent;
  let fixture: ComponentFixture<FamilyHealthComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FamilyHealthComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FamilyHealthComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
