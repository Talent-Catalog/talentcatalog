import {Component, EventEmitter, Input, Output, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {of, throwError} from 'rxjs';
import {DuolingoComponent} from './duolingo.component';
import {DuolingoCouponService} from '../../../../../../services/duolingo-coupon.service';
import {DuolingoCouponStatus} from '../../../../../../model/duolingo-coupon';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({selector: 'app-error', template: ''})
class ErrorStubComponent {
  @Input() error: unknown;
}

@Component({selector: 'tc-loading', template: ''})
class TcLoadingStubComponent {
  @Input() loading = false;
}

@Component({selector: 'app-duolingo-coupon', template: ''})
class DuolingoCouponStubComponent {
  @Input() candidate: unknown;
  @Input() selectedCoupon: unknown;
  @Input() activeDuolingoTask: unknown;
  @Output() refresh = new EventEmitter<void>();
}

describe('DuolingoComponent', () => {
  let component: DuolingoComponent;
  let fixture: ComponentFixture<DuolingoComponent>;
  let duolingoCouponServiceSpy: jasmine.SpyObj<DuolingoCouponService>;

  const makeCandidate = () => ({
    id: 12,
    candidateExams: [
      {exam: 'DETOfficial', score: '130', year: '2025'},
      {exam: 'IELTSGen', score: '7', year: '2024'},
    ],
  } as any);

  beforeEach(async () => {
    duolingoCouponServiceSpy = jasmine.createSpyObj('DuolingoCouponService', ['getCouponsForCandidate']);
    duolingoCouponServiceSpy.getCouponsForCandidate.and.returnValue(of([
      {couponCode: 'ABC123', duolingoCouponStatus: DuolingoCouponStatus.SENT},
      {couponCode: 'HIDE', duolingoCouponStatus: DuolingoCouponStatus.REDEEMED},
    ] as any));

    await TestBed.configureTestingModule({
      declarations: [
        DuolingoComponent,
        TranslatePipeStub,
        ErrorStubComponent,
        TcLoadingStubComponent,
        DuolingoCouponStubComponent,
      ],
      providers: [
        {provide: DuolingoCouponService, useValue: duolingoCouponServiceSpy},
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DuolingoComponent);
    component = fixture.componentInstance;
    component.candidate = makeCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render tc-loading and the available coupon cards', () => {
    const loading = fixture.debugElement.query(By.directive(TcLoadingStubComponent));

    expect(loading).toBeTruthy();
    expect(component.coupons).toEqual(['ABC123']);
    expect(fixture.nativeElement.textContent).toContain('SERVICES.DUOLINGO.COUPONS_TITLE');
    expect(fixture.nativeElement.querySelectorAll('.ticket-container').length).toBe(1);
  });

  it('should render the coupon child when a coupon is selected', () => {
    component.selectCoupon('ABC123');
    fixture.detectChanges();

    const child = fixture.debugElement.query(By.directive(DuolingoCouponStubComponent));
    expect(child).toBeTruthy();
    expect((child.componentInstance as DuolingoCouponStubComponent).selectedCoupon).toBe('ABC123');
    expect((child.componentInstance as DuolingoCouponStubComponent).candidate).toBe(component.candidate);
  });

  it('should expose only DET exams in past exams', () => {
    expect(component.detExams.length).toBe(1);
    expect(component.detExams[0].exam).toBe('DETOfficial');
  });

  it('should set error when coupon loading fails', () => {
    duolingoCouponServiceSpy.getCouponsForCandidate.and.returnValue(throwError('coupon-error'));
    component.fetchCoupons();

    expect(component.error).toBe('coupon-error');
    expect(component.loading).toBeFalse();
  });
});
