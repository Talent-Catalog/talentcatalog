import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from '../../../../../../model/candidate';
import {DuolingoCouponService} from '../../../../../../services/duolingo-coupon.service';
import {DuolingoCouponStatus} from '../../../../../../model/duolingo-coupon';
import {TaskAssignment} from "../../../../../../model/task-assignment";
import { CandidateExam, Exam } from '../../../../../../model/candidate'

@Component({
    selector: 'app-duolingo',
    templateUrl: './duolingo.component.html',
    styleUrls: ['./duolingo.component.scss']
})
export class DuolingoComponent implements OnInit {
    @Input() candidate: Candidate;
    @Input() activeDuolingoTask: TaskAssignment;
    selectedCoupon: Object;
    coupons: string[];
    loading: boolean;
    error;
    detExams: CandidateExam[];
    @Output() refresh = new EventEmitter();

    constructor(
        private duolingoCouponService: DuolingoCouponService,
    ) {

    }

    ngOnInit(): void {
        this.fetchCoupons();
        this.detExams = this.getDuolingoCandidateExams();
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

    selectCoupon(selectedCoupon: Object) {
        this.selectedCoupon = selectedCoupon;
        console.log('selectedCoupon', this.selectedCoupon);
    }

    unSelectCoupon() {
        this.selectedCoupon = null;
        console.log(this.selectedCoupon);
        this.refresh.emit();
    }

  getDuolingoCandidateExams(): CandidateExam[] {
    return this.candidate?.candidateExams?.filter(
      exam => exam.exam === Exam.DETOfficial
    ) ?? [];
  }
}
