import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcPaginationComponent} from './tc-pagination.component';
import {NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";

describe('TcPaginationComponent', () => {
  let component: TcPaginationComponent;
  let fixture: ComponentFixture<TcPaginationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TcPaginationComponent],
      imports: [NgbPaginationModule]
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
    expect(totalEl.textContent).toContain('Found 25 in total');
  });

  it('should update pageNumber and emit on valid page change', () => {
    spyOn(component.pageChange, 'emit');
    component.onPageChange(2);
    expect(component.pageNumber).toBe(2);
    expect(component.pageChange.emit).toHaveBeenCalledWith(2);
  });

  it('should not emit on invalid page number (<1)', () => {
    spyOn(component.pageChange, 'emit');
    component.onPageChange(0);
    expect(component.pageChange.emit).not.toHaveBeenCalled();
  });

  it('selectPage should call onPageChange with parsed number', () => {
    spyOn(component, 'onPageChange');
    component.selectPage('3');
    expect(component.onPageChange).toHaveBeenCalledWith(3);
  });

  it('selectPage should default to 1 if parsing fails', () => {
    spyOn(component, 'onPageChange');
    component.selectPage('abc');
    expect(component.onPageChange).toHaveBeenCalledWith(1);
  });

  it('formatInput should strip non-numeric chars', () => {
    const input = document.createElement('input');
    input.value = 'abc123';
    component.formatInput(input);
    expect(input.value).toBe('123');
  });

  it('should call selectPage on Enter key', () => {
    spyOn(component, 'selectPage');
    fixture.detectChanges();
    const input = fixture.nativeElement.querySelector('#paginationInput');
    input.value = '4';
    input.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter' }));
    expect(component.selectPage).toHaveBeenCalledWith('4');
  });

  it('should call selectPage on blur', () => {
    spyOn(component, 'selectPage');
    fixture.detectChanges();
    const input = fixture.nativeElement.querySelector('#paginationInput');
    input.value = '4';
    input.dispatchEvent(new Event('blur'));
    expect(component.selectPage).toHaveBeenCalledWith('4');
  });
});
