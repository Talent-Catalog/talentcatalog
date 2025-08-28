import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DescriptionTermComponent } from './description-term.component';

describe('DescriptionTermComponent', () => {
  let component: DescriptionTermComponent;
  let fixture: ComponentFixture<DescriptionTermComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DescriptionTermComponent]
    });
    fixture = TestBed.createComponent(DescriptionTermComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
