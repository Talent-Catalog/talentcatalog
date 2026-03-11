import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LinkedinRedeemedComponent } from './linkedin-redeemed.component';

describe('LinkedinRedeemedComponent', () => {
  let component: LinkedinRedeemedComponent;
  let fixture: ComponentFixture<LinkedinRedeemedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LinkedinRedeemedComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LinkedinRedeemedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
