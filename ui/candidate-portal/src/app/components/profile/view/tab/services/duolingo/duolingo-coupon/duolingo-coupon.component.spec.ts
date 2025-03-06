import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DuolingoCouponComponent } from './duolingo-coupon.component';

describe('DuolingoCouponComponent', () => {
  let component: DuolingoCouponComponent;
  let fixture: ComponentFixture<DuolingoCouponComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DuolingoCouponComponent]
    });
    fixture = TestBed.createComponent(DuolingoCouponComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
