import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TcCardHeaderComponent } from './tc-card-header.component';

describe('TcCardHeaderComponent', () => {
  let component: TcCardHeaderComponent;
  let fixture: ComponentFixture<TcCardHeaderComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcCardHeaderComponent]
    });
    fixture = TestBed.createComponent(TcCardHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
