import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BuildLinkComponent } from './build-link.component';

describe('LinkCreatorComponent', () => {
  let component: BuildLinkComponent;
  let fixture: ComponentFixture<BuildLinkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BuildLinkComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BuildLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
