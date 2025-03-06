import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-duolingo-coupon',
  templateUrl: './duolingo-coupon.component.html',
  styleUrls: ['./duolingo-coupon.component.scss'],
})
export class DuolingoCouponComponent {
  @Input() selectedCoupon: Object;
  loading: boolean;
  error;
  isCollapsedIntro = false;
  isCollapsedSteps = false;

  constructor() {
  }
}
