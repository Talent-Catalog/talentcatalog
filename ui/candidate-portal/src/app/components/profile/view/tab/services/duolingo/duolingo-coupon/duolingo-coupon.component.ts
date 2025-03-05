import {Component, Input} from '@angular/core';
import {Candidate} from '../../../../../../../model/candidate';
import {NgbCollapseModule} from '@ng-bootstrap/ng-bootstrap';
import {faChevronUp, faChevronDown} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-duolingo-coupon',
  templateUrl: './duolingo-coupon.component.html',
  styleUrls: ['./duolingo-coupon.component.scss'],
  imports: [NgbCollapseModule],
})
export class DuolingoCouponComponent {
  @Input() selectedCoupon: Object;
  loading: boolean;
  error;
  isCollapsedIntro = false;
  isCollapsedSteps = false;
  faChevronUp = faChevronUp;
  faChevronDown = faChevronDown;

  constructor() {
  }
}
