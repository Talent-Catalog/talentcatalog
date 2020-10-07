import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ReturnHomeSafeComponent} from './return-home-safe.component';

describe('HomeSafeComponent', () => {
  let component: ReturnHomeSafeComponent;
  let fixture: ComponentFixture<ReturnHomeSafeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReturnHomeSafeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReturnHomeSafeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
