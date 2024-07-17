import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PreviewLinkComponent } from './preview-link.component';

describe('PreviewLinkComponent', () => {
  let component: PreviewLinkComponent;
  let fixture: ComponentFixture<PreviewLinkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PreviewLinkComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PreviewLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
