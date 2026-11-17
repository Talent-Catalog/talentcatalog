import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcCardHeaderComponent} from './tc-card-header.component';
import {expect} from 'vitest';

describe('TcCardHeaderComponent', () => {
  let component: TcCardHeaderComponent;
  let fixture: ComponentFixture<TcCardHeaderComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TcCardHeaderComponent]
    });
    fixture = TestBed.createComponent(TcCardHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
