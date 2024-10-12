import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FindListComponent} from './find-list.component';

describe('FindListComponent', () => {
  let component: FindListComponent;
  let fixture: ComponentFixture<FindListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FindListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FindListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
