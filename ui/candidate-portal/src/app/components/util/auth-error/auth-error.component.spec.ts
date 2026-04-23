import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthErrorComponent } from './auth-error.component';

describe('AuthErrorComponent', () => {
  let component: AuthErrorComponent;
  let fixture: ComponentFixture<AuthErrorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthErrorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AuthErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
