import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SelectListComponent} from './select-list.component';

describe('SelectListComponent', () => {
  let component: SelectListComponent;
  let fixture: ComponentFixture<SelectListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
