import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from '../../../../../../model/candidate';
import {DuolingoCouponService} from '../../../../../../services/duolingo-coupon.service';
import {DuolingoCouponStatus} from '../../../../../../model/duolingo-coupon';

@Component({
    selector: 'app-duolingo',
    templateUrl: './duolingo.component.html',
    styleUrls: ['./duolingo.component.scss']
})
export class DuolingoComponent implements OnInit {
    @Input() candidate: Candidate;
    selectedCoupon: Object;
    coupons: string[];
    loading: boolean;
    error;
    @Output() refresh = new EventEmitter();

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
                console.log(coupons);
                this.coupons = coupons.filter(coupon => coupon.duolingoCouponStatus === DuolingoCouponStatus.SENT).map(coupon => coupon.couponCode);
                console.log(this.coupons);

                this.loading = false;
            },
            error => {
                this.error = error;
                this.loading = false;
            }
        );
    }

    selectCoupon(selectedCoupon: Object) {
        this.selectedCoupon = selectedCoupon;
        console.log('selectedCoupon', this.selectedCoupon);
    }

    unSelectCoupon() {
        this.selectedCoupon = null;
        console.log(this.selectedCoupon);
        this.refresh.emit();
    }


}
