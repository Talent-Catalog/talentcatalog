import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TcLoadingComponent } from './tc-loading.component';

describe('TcLoadingComponent', () => {
  let component: TcLoadingComponent;
  let fixture: ComponentFixture<TcLoadingComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TcLoadingComponent]
    });
    fixture = TestBed.createComponent(TcLoadingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render nothing when not loading', () => {
    expect(fixture.nativeElement.querySelector('.tc-loader')).toBeNull();
  });

  (['page', 'section'] as const).forEach(type => {
    describe(`${type} loader`, () => {
      beforeEach(() => {
        fixture.componentRef.setInput('loading', true);
        fixture.componentRef.setInput('type', type);
        fixture.detectChanges();
      });

      it(`should apply the tc-loader-${type} class`, () => {
        const loader: HTMLElement = fixture.nativeElement.querySelector('.tc-loader');
        expect(loader.classList).toContain(`tc-loader-${type}`);
      });

      it('should render the logo as an inline SVG inside the ring', () => {
        const svg: SVGElement = fixture.nativeElement.querySelector('.tc-loader-ring svg.tc-loader-logo');
        expect(svg).not.toBeNull();
        expect(svg.getAttribute('viewBox')).toBe('0 0 176 128');
        expect(svg.querySelectorAll('path').length).toBe(6);
      });

      it('should not load the logo from an image file', () => {
        expect(fixture.nativeElement.querySelector('img')).toBeNull();
      });
    });
  });

  it('should show "Loading page" text for the page loader', () => {
    fixture.componentRef.setInput('loading', true);
    fixture.componentRef.setInput('type', 'page');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('p').textContent).toContain('Loading page');
  });
});
