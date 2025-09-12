import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcAccordionItemComponent} from './tc-accordion-item.component';

describe('TcAccordionItemComponent', () => {
  let component: TcAccordionItemComponent;
  let fixture: ComponentFixture<TcAccordionItemComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcAccordionItemComponent]
    });
    fixture = TestBed.createComponent(TcAccordionItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
