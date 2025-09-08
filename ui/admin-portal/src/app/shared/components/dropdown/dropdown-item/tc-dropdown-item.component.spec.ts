import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DropdownItemComponent} from './tc-dropdown-item.component';

describe('DropdownItemComponent', () => {
  let component: DropdownItemComponent;
  let fixture: ComponentFixture<DropdownItemComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DropdownItemComponent]
    });
    fixture = TestBed.createComponent(DropdownItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
