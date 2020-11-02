import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ReturnHomeFutureComponent} from './return-home-future.component';

describe('ReturnHomeFutureComponent', () => {
  let component: ReturnHomeFutureComponent;
  let fixture: ComponentFixture<ReturnHomeFutureComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReturnHomeFutureComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReturnHomeFutureComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
