import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TermsComponent } from './terms.component';

describe('PrivacyPolicyComponent', () => {
  let component: TermsComponent;
  let fixture: ComponentFixture<TermsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TermsComponent]
    });
    fixture = TestBed.createComponent(TermsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
