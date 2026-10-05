import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TcCardComponent } from './tc-card.component';

describe('TcCardComponent', () => {
  let component: TcCardComponent;
  let fixture: ComponentFixture<TcCardComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TcCardComponent]
    });
    fixture = TestBed.createComponent(TcCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
