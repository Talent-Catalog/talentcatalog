import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LinkTooltipComponent } from './link-tooltip.component';

describe('LinkTooltipComponent', () => {
  let component: LinkTooltipComponent;
  let fixture: ComponentFixture<LinkTooltipComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LinkTooltipComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LinkTooltipComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
