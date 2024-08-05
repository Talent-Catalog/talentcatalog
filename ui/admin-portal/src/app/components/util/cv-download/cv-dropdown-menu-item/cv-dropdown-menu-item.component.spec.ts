import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CvDropdownMenuItemComponent } from './cv-dropdown-menu-item.component';

describe('CvDropdownMenuItemComponent', () => {
  let component: CvDropdownMenuItemComponent;
  let fixture: ComponentFixture<CvDropdownMenuItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CvDropdownMenuItemComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CvDropdownMenuItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
