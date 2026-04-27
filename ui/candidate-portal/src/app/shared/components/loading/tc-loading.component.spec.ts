import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TcLoadingComponent } from './tc-loading.component';

describe('TcLoadingComponent', () => {
  let component: TcLoadingComponent;
  let fixture: ComponentFixture<TcLoadingComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcLoadingComponent]
    });
    fixture = TestBed.createComponent(TcLoadingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
