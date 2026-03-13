import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {DuolingoCouponService} from '../../../services/duolingo-coupon.service';
import {ImportDuolingoCouponsComponent} from './import-duolingo-coupons.component';

describe('ImportDuolingoCouponsComponent', () => {
  let component: ImportDuolingoCouponsComponent;
  let fixture: ComponentFixture<ImportDuolingoCouponsComponent>;
  let mockDuolingoCouponService: jasmine.SpyObj<DuolingoCouponService>;

  beforeEach(async () => {
    mockDuolingoCouponService = jasmine.createSpyObj('DuolingoCouponService', [
      'importCoupons',
      'countAvailableProctoredCoupons'
    ]);

    await TestBed.configureTestingModule({
      declarations: [ImportDuolingoCouponsComponent],
      providers: [{provide: DuolingoCouponService, useValue: mockDuolingoCouponService}],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ImportDuolingoCouponsComponent);
    component = fixture.componentInstance;
    mockDuolingoCouponService.countAvailableProctoredCoupons.and.returnValue(of({count: 100}));
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set an error message on service failure', () => {
    component.selectedFile = new File([], 'coupons.csv');
    mockDuolingoCouponService.importCoupons.and.returnValue(throwError(() => new Error('Error')));

    component.importCSV();

    expect(component.working).toBeFalse();
    expect(component.error).toBe('Failed to import the CSV file. Please try again.');
  });

  it('should set an error message if no file is selected', () => {
    component.selectedFile = null;

    component.importCSV();

    expect(component.error).toBe('Please select a file to import.');
  });
});
