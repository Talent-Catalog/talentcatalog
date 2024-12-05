import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DuplicatesDetailComponent } from './duplicates-detail.component';

describe('DuplicatesDetailComponent', () => {
  let component: DuplicatesDetailComponent;
  let fixture: ComponentFixture<DuplicatesDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DuplicatesDetailComponent]
    });
    fixture = TestBed.createComponent(DuplicatesDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
