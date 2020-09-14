import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ReturnedHomeComponent} from './returned-home.component';

describe('ReturnedHomeComponent', () => {
  let component: ReturnedHomeComponent;
  let fixture: ComponentFixture<ReturnedHomeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReturnedHomeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReturnedHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
