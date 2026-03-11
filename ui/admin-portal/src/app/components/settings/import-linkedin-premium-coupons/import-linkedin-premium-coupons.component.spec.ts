import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ImportLinkedinPremiumCouponsComponent} from './import-linkedin-premium-coupons.component';
import {of, throwError} from 'rxjs';
import {NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {LinkedinPremiumCouponService} from "../../../services/linkedin-premium-coupon.service";

describe('ImportDuolingoCouponsComponent', () => {
  let component: ImportLinkedinPremiumCouponsComponent;
  let fixture: ComponentFixture<ImportLinkedinPremiumCouponsComponent>;
  let mockCouponService: jasmine.SpyObj<LinkedinPremiumCouponService>;

  beforeEach(async () => {
    mockCouponService = jasmine.createSpyObj('DuolingoCouponService', ['importCoupons','countAvailableCoupons']);

    await TestBed.configureTestingModule({
      imports: [NgbPaginationModule],
      declarations: [ImportLinkedinPremiumCouponsComponent],
      providers: [{provide: LinkedinPremiumCouponService, useValue: mockCouponService}]
    }).compileComponents();

    fixture = TestBed.createComponent(ImportLinkedinPremiumCouponsComponent);
    component = fixture.componentInstance;
    mockCouponService.countAvailableCoupons =
      jasmine.createSpy('countAvailableCoupons').and.returnValue(of(100)); // Mock return value if needed

    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('onFileChange', () => {
    it('should parse the file and update data when a valid file is selected', () => {
      const csvFile = new Blob(
        [
          'Serial #,Premium Code,Activate by\n8292,https://www.linkedin.com/premium/redeem/promo?upsellOrderOrigin=lbp_vs&coupon=x3ebuXYbQ&customKey=vs_generic&redeemTypeV2=PREPAID_COUPON,11-11-2026',
        ],
        {type: 'text/csv'}
      );
      const fileEvent = {target: {files: [new File([csvFile], 'coupons.csv')]}} as any;

      spyOn(component, 'parseCSV').and.callThrough();
      component.onFileChange(fileEvent);

      expect(component.parseCSV).toHaveBeenCalledWith(jasmine.any(File));
    });

    it('should not parse the file when no file is selected', () => {
      const fileEvent = {target: {files: []}} as any;

      spyOn(component, 'parseCSV');
      component.onFileChange(fileEvent);

      expect(component.parseCSV).not.toHaveBeenCalled();
    });
  });

  describe('importCSV', () => {
    it('should set an error message on service failure', () => {
      component.selectedFile = new File([], 'coupons.csv');
      mockCouponService.importCoupons.and.returnValue(throwError(() => new Error('Error')));

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

  describe('pagination', () => {
    it('should update paginatedData when onPageChange is called', () => {
      component.csvData = Array.from({length: 100}, (_, i) => [`Row${i + 1}`]);
      component.pageSize = 30;

      component.onPageChange(2);

      expect(component.currentPage).toBe(2);
      expect(component.paginatedData.length).toBe(30);
      expect(component.paginatedData[0]).toEqual(['Row31']);
    });

    it('should slice csvData correctly when updatePaginatedData is called', () => {
      component.csvData = Array.from({length: 100}, (_, i) => [`Row${i + 1}`]);
      component.pageSize = 30;
      component.currentPage = 3;

      component.updatePaginatedData();

      expect(component.paginatedData.length).toBe(30);
      expect(component.paginatedData[0]).toEqual(['Row61']);
    });
  });
});
