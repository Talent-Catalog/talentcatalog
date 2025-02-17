import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from "../../../../../../model/candidate";
import {DuolingoCouponService} from "../../../../../../services/duolingo-coupon.service";
import {DuolingoCouponStatus} from "../../../../../../model/duolingo-coupon";

@Component({
  selector: 'app-duolingo-coupon',
  templateUrl: './duolingo-coupon.component.html',
  styleUrls: ['./duolingo-coupon.component.scss']
})
export class DuolingoCouponComponent implements OnInit {
  @Input() selectedService: String;
  @Input() candidate: Candidate;
  @Output() back = new EventEmitter();
  coupons: string[];
  loading: boolean;
  error;

  constructor(
    private duolingoCouponService: DuolingoCouponService,
  ) {
  }

  ngOnInit(): void {
    this.fetchCoupons();
  }

  fetchCoupons() {
    this.loading = true;
    this.duolingoCouponService.getCouponsForCandidate(this.candidate.id).subscribe(
      coupons => {
        this.coupons = coupons.filter(coupon => coupon.duolingoCouponStatus === DuolingoCouponStatus.SENT).map(coupon => coupon.couponCode);
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  goBack() {
    this.back.emit();
  }

}
