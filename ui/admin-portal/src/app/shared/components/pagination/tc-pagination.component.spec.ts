import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcPaginationComponent} from './tc-pagination.component';
import {NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {expect, vi} from 'vitest';

describe('TcPaginationComponent', () => {
    let component: TcPaginationComponent;
    let fixture: ComponentFixture<TcPaginationComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TcPaginationComponent, NgbPaginationModule]
        }).compileComponents();

        fixture = TestBed.createComponent(TcPaginationComponent);
        component = fixture.componentInstance;
        component.totalElements = 50;
        component.pageSize = 10;
        component.pageNumber = 1;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display total elements count', () => {
        component.totalElements = 25;
        fixture.detectChanges();
        const totalEl = fixture.nativeElement.querySelector('.results-total');
        expect(totalEl.textContent).toContain('25 total');
    });

    it('should update pageNumber and emit on valid page change', () => {
        vi.spyOn(component.pageChange, 'emit').mockReturnValue(undefined);
        component.onPageChange(2);
        expect(component.pageNumber).toBe(2);
        expect(component.pageChange.emit).toHaveBeenCalledWith(2);
    });

    it('should not emit on invalid page number (<1)', () => {
        vi.spyOn(component.pageChange, 'emit').mockReturnValue(undefined);
        component.onPageChange(0);
        expect(component.pageChange.emit).not.toHaveBeenCalled();
    });

    it('selectPage should call onPageChange with parsed number', () => {
        vi.spyOn(component, 'onPageChange').mockReturnValue(undefined);
        component.selectPage('3');
        expect(component.onPageChange).toHaveBeenCalledWith(3);
    });

    it('should default to page 1 when parsing fails and current page differs', () => {
      component.pageNumber = 2;

      const pageChangeSpy = vi.spyOn(component, 'onPageChange');

      component.selectPage('abc');

      expect(pageChangeSpy).toHaveBeenCalledWith(1);
    });

    it('formatInput should strip non-numeric chars', () => {
        const input = document.createElement('input');
        input.value = 'abc123';
        component.formatInput(input);
        expect(input.value).toBe('123');
    });

    it('should call selectPage on Enter key', () => {
        vi.spyOn(component, 'selectPage').mockReturnValue(undefined);
        fixture.detectChanges();
        const input = fixture.nativeElement.querySelector('#paginationInput');
        input.value = '4';
        input.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter' }));
        expect(component.selectPage).toHaveBeenCalledWith('4');
    });

    it('should call selectPage on blur', () => {
        vi.spyOn(component, 'selectPage').mockReturnValue(undefined);
        fixture.detectChanges();
        const input = fixture.nativeElement.querySelector('#paginationInput');
        input.value = '4';
        input.dispatchEvent(new Event('blur'));
        expect(component.selectPage).toHaveBeenCalledWith('4');
    });

    it('should not search when the current page is selected again', () => {
      const emitSpy = vi.spyOn(component.pageChange, 'emit');

      component.selectPage('1');

      expect(emitSpy).not.toHaveBeenCalled();
    });

    it('should emit when a different page is selected', () => {
      const emitSpy = vi.spyOn(component.pageChange, 'emit');

      component.selectPage('2');

      expect(emitSpy).toHaveBeenCalledExactlyOnceWith(2);
    });

    it('should not emit when the page input loses focus unchanged', () => {
      const emitSpy = vi.spyOn(component.pageChange, 'emit');
      const input = fixture.nativeElement.querySelector('#paginationInput');

      input.value = '1';
      input.dispatchEvent(new Event('blur'));

      expect(emitSpy).not.toHaveBeenCalled();
    });

    it('should not emit a page change when selecting the current page', () => {
      const emitSpy = vi.spyOn(component.pageChange, 'emit');

      component.selectPage('1');

      expect(emitSpy).not.toHaveBeenCalled();
    });
});
