import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AdminApiComponent} from './admin-api.component';

describe('AdminApiComponent', () => {
  let component: AdminApiComponent;
  let fixture: ComponentFixture<AdminApiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminApiComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminApiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
