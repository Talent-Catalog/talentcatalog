import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ButtonComponent} from './button.component';
import {By} from '@angular/platform-browser';

describe('ButtonComponent', () => {
  let component: ButtonComponent;
  let fixture: ComponentFixture<ButtonComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ButtonComponent]
    });
    fixture = TestBed.createComponent(ButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should apply correct classes based on size and type inputs', () => {
    component.size = 'sm';
    component.type = 'outline';
    fixture.detectChanges();

    const buttonElement: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(buttonElement.classList).toContain('btn-sm');
    expect(buttonElement.classList).toContain('btn-outline');
  });

  it('should apply "disabled" attribute when disabled is true', () => {
    component.disabled = true;
    fixture.detectChanges();

    const buttonElement: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(buttonElement.disabled).toBeTrue();
  });

  describe('aria-label support', () => {
    beforeEach(() => {
      // Only needed if you implement @Input() ariaLabel
      (component as any).ariaLabel = 'Refresh';
      fixture.detectChanges();
    });

    it('should set aria-label when ariaLabel input is provided', () => {
      const buttonDebug = fixture.debugElement.query(By.css('button'));
      expect(buttonDebug.attributes['aria-label']).toBe('Refresh');
    });
  });

  describe('loading state', () => {
    it('should render spinner icon when loading is true', () => {
      component.loading = true;
      fixture.detectChanges();

      const spinner = fixture.debugElement.query(By.css('i.fas.fa-spinner.fa-spin'));
      expect(spinner).toBeTruthy();
    });

    it('should disable the native button when loading is true', () => {
      component.loading = true;
      fixture.detectChanges();

      const buttonElement: HTMLButtonElement = fixture.nativeElement.querySelector('button');
      expect(buttonElement.disabled).toBeTrue();
    });

    it('should add btn-loading class when loading is true', () => {
      component.loading = true;
      fixture.detectChanges();

      const buttonElement: HTMLButtonElement = fixture.nativeElement.querySelector('button');
      expect(buttonElement.classList).toContain('btn-loading');
    });

    it('should not emit onClick when loading is true', () => {
      component.loading = true;
      fixture.detectChanges();

      spyOn(component.onClick, 'emit');

      const buttonDebug = fixture.debugElement.query(By.css('button'));
      buttonDebug.triggerEventHandler('click', new MouseEvent('click'));

      expect(component.onClick.emit).not.toHaveBeenCalled();
    });

    it('should emit onClick when loading is false and disabled is false', () => {
      component.loading = false;
      component.disabled = false;
      fixture.detectChanges();

      spyOn(component.onClick, 'emit');

      const buttonDebug = fixture.debugElement.query(By.css('button'));
      buttonDebug.triggerEventHandler('click', new MouseEvent('click'));

      expect(component.onClick.emit).toHaveBeenCalledTimes(1);
    });
  });

  describe('link mode', () => {
    it('should render an anchor when href is provided', () => {
      component.href = 'https://example.com';
      component.target = '_blank';
      fixture.detectChanges();

      const linkElement: HTMLAnchorElement = fixture.nativeElement.querySelector('a');
      const buttonElement: HTMLButtonElement = fixture.nativeElement.querySelector('button');

      expect(linkElement).toBeTruthy();
      expect(linkElement.getAttribute('href')).toBe('https://example.com');
      expect(linkElement.getAttribute('target')).toBe('_blank');
      expect(linkElement.getAttribute('rel')).toBe('noopener noreferrer');
      expect(buttonElement).toBeFalsy();
    });

    it('should use explicit rel when provided', () => {
      component.href = 'https://example.com';
      component.target = '_blank';
      component.rel = 'nofollow';
      fixture.detectChanges();

      const linkElement: HTMLAnchorElement = fixture.nativeElement.querySelector('a');
      expect(linkElement.getAttribute('rel')).toBe('nofollow');
    });

    it('should not emit onClick for disabled link mode', () => {
      component.href = 'https://example.com';
      component.disabled = true;
      fixture.detectChanges();
      spyOn(component.onClick, 'emit');

      const linkDebug = fixture.debugElement.query(By.css('a'));
      const event = jasmine.createSpyObj<MouseEvent>('event', ['preventDefault', 'stopPropagation']);
      component.clicked(event);

      expect(component.onClick.emit).not.toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(linkDebug.attributes['aria-disabled']).toBe('true');
    });

    it('should disable link navigation when loading is true', () => {
      component.href = 'https://example.com';
      component.loading = true;
      fixture.detectChanges();

      const linkElement: HTMLAnchorElement = fixture.nativeElement.querySelector('a');
      expect(linkElement.getAttribute('href')).toBeNull();
    });
  });
});
