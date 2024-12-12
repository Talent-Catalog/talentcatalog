import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ImportDuolingoCouponsComponent} from './import-duolingo-coupons.component';
import {DuolingoCouponService} from '../../../services/duolingo-coupon.service';
import {throwError} from 'rxjs';
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

describe('ImportDuolingoCouponsComponent', () => {
  let component: ImportDuolingoCouponsComponent;
  let fixture: ComponentFixture<ImportDuolingoCouponsComponent>;
  let mockDuolingoCouponService: jasmine.SpyObj<DuolingoCouponService>;

  beforeEach(async () => {
    mockDuolingoCouponService = jasmine.createSpyObj('DuolingoCouponService', ['importCoupons']);

    await TestBed.configureTestingModule({
      declarations: [ImportDuolingoCouponsComponent],
      providers: [{provide: DuolingoCouponService, useValue: mockDuolingoCouponService}],
      schemas: [CUSTOM_ELEMENTS_SCHEMA], // Add this only if absolutely necessary
    }).compileComponents();

    fixture = TestBed.createComponent(ImportDuolingoCouponsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('onFileChange', () => {
    it('should parse the file and update data when a valid file is selected', () => {
      const csvFile = new Blob(
        [
          'Coupon Code,Expiration Date,Date Sent,Coupon Status\nCODE123,2024-12-31,2024-11-01,Active',
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
