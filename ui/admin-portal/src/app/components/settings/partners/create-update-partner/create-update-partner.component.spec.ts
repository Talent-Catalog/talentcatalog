import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateUpdatePartnerComponent } from './create-update-partner.component';

describe('CreateUpdatePartnerComponent', () => {
  let component: CreateUpdatePartnerComponent;
  let fixture: ComponentFixture<CreateUpdatePartnerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateUpdatePartnerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUpdatePartnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
