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

      it('should render the SVG logo inside the ring', () => {
        const logo: HTMLImageElement = fixture.nativeElement.querySelector('.tc-loader-ring img.tc-loader-logo');
        expect(logo).not.toBeNull();
        expect(logo.getAttribute('src')).toBe('assets/images/tc-logo.svg');
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
