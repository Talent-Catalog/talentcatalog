import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PresetEmbedComponent } from './preset-embed.component';

describe('PresetEmbedComponent', () => {
  let component: PresetEmbedComponent;
  let fixture: ComponentFixture<PresetEmbedComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PresetEmbedComponent]
    });
    fixture = TestBed.createComponent(PresetEmbedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
