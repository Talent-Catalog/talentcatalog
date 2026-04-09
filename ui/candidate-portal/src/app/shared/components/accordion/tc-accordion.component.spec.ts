import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcAccordionComponent} from './tc-accordion.component';

describe('TcAccordionComponent', () => {
  let component: TcAccordionComponent;
  let fixture: ComponentFixture<TcAccordionComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcAccordionComponent]
    });
    fixture = TestBed.createComponent(TcAccordionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
