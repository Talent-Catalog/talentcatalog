import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcDropdownDividerComponent} from './tc-dropdown-divider.component';
import {expect} from 'vitest';

describe('TcDropdownDividerComponent', () => {
  let component: TcDropdownDividerComponent;
  let fixture: ComponentFixture<TcDropdownDividerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TcDropdownDividerComponent]
    });
    fixture = TestBed.createComponent(TcDropdownDividerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
