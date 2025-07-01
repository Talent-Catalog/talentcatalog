import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcPaginationComponent} from './tc-pagination.component';

describe('TcPaginationComponent', () => {
  let component: TcPaginationComponent;
  let fixture: ComponentFixture<TcPaginationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcPaginationComponent]
    });
    fixture = TestBed.createComponent(TcPaginationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
