import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewPrivacyPolicyInfoComponent } from './view-privacy-policy-info.component';
import {HttpClientModule} from "@angular/common/http";

describe('PrivacyPolicyInfoComponent', () => {
  let component: ViewPrivacyPolicyInfoComponent;
  let fixture: ComponentFixture<ViewPrivacyPolicyInfoComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      declarations: [ViewPrivacyPolicyInfoComponent]
    });
    fixture = TestBed.createComponent(ViewPrivacyPolicyInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
