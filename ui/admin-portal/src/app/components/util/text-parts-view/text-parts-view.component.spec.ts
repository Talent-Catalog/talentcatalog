import {Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {TextPartsViewComponent} from './text-parts-view.component';

@Component({
  standalone: true,
  imports: [TextPartsViewComponent],
  template: `
    <app-text-parts-view [text]="text"></app-text-parts-view>
  `
})
class HostComponent {
  text: string | null = null;
}

describe('TextPartsViewComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HostComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
  });

  it('displays legacy plain text as original', () => {
    host.text = 'Candidate entered text';
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;

    expect(element.textContent).toContain('Original');
    expect(element.textContent).toContain('Candidate entered text');
    expect(element.textContent).not.toContain('Tidied');
    expect(element.textContent).not.toContain('Keywords');
  });

  it('displays original, tidied and keywords from JSON text parts', () => {
    host.text = JSON.stringify({
      parts: {
        original: '<p>i work electrician</p>',
        tidied: '<p>I worked as an electrician.</p>',
        keywords: ['electrician', 'wiring']
      }
    });

    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;

    expect(element.textContent).toContain('Original');
    expect(element.textContent).toContain('i work electrician');

    expect(element.textContent).toContain('Tidied');
    expect(element.textContent).toContain('I worked as an electrician.');

    expect(element.textContent).toContain('Keywords');
    expect(element.textContent).toContain('electrician');
    expect(element.textContent).toContain('wiring');
  });

  it('renders HTML content using innerHTML', () => {
    host.text = JSON.stringify({
      parts: {
        original: '<p>Original <strong>HTML</strong></p>',
        tidied: '<p>Tidied <em>HTML</em></p>',
        keywords: []
      }
    });

    fixture.detectChanges();

    const htmlElements = fixture.debugElement.queryAll(By.css('strong, em'));

    expect(htmlElements.length).toBe(2);
    expect(fixture.nativeElement.textContent).toContain('Original HTML');
    expect(fixture.nativeElement.textContent).toContain('Tidied HTML');
  });

  it('does not display empty tidied or keywords sections', () => {
    host.text = JSON.stringify({
      parts: {
        original: 'Original only',
        tidied: '',
        keywords: []
      }
    });

    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;

    expect(element.textContent).toContain('Original only');
    expect(element.textContent).not.toContain('Tidied');
    expect(element.textContent).not.toContain('Keywords');
  });

  it('treats malformed JSON as legacy original text', () => {
    host.text = '{"parts":{"original":"hello\nworld"}}';
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;

    expect(element.textContent).toContain('Original');
    expect(element.textContent).toContain('hello');
    expect(element.textContent).toContain('world');
  });

  it('updates when input text changes', () => {
    host.text = 'First value';
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('First value');

    host.text = JSON.stringify({
      parts: {
        original: 'Second original',
        tidied: 'Second tidied',
        keywords: ['second']
      }
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Second original');
    expect(fixture.nativeElement.textContent).toContain('Second tidied');
    expect(fixture.nativeElement.textContent).toContain('second');
  });
});
