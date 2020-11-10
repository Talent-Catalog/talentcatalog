import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DependantsComponent} from './dependants.component';

describe('DependantsComponent', () => {
  let component: DependantsComponent;
  let fixture: ComponentFixture<DependantsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DependantsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DependantsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
