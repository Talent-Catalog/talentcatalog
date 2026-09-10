/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {CommonModule} from '@angular/common';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {of} from 'rxjs';

import {LinkPreview} from '../../../model/link-preview';
import {LinkPreviewService} from '../../../services/link-preview.service';
import {PreviewLinkComponent} from './preview-link.component';

describe('PreviewLinkComponent', () => {
  let component: PreviewLinkComponent;
  let fixture: ComponentFixture<PreviewLinkComponent>;
  let linkPreviewServiceSpy: jasmine.SpyObj<LinkPreviewService>;

  beforeEach(async () => {
    linkPreviewServiceSpy =
      jasmine.createSpyObj<LinkPreviewService>(
        'LinkPreviewService',
        ['delete']
      );

    linkPreviewServiceSpy.delete.and.returnValue(of(true));

    await TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [PreviewLinkComponent],
      providers: [
        {
          provide: LinkPreviewService,
          useValue: linkPreviewServiceSpy
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PreviewLinkComponent);
    component = fixture.componentInstance;

    component.linkPreview = createLinkPreview();
    component.userIsPostAuthor = true;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialise without changing the supplied preview', () => {
    const preview = component.linkPreview;

    expect(() => component.ngOnInit()).not.toThrow();
    expect(component.linkPreview).toBe(preview);
  });

  it('should render the link preview content', () => {
    const anchor = fixture.debugElement.query(
      By.css('a.link-wrapper')
    );

    const domain = fixture.debugElement.query(
      By.css('.domain')
    );

    const title = fixture.debugElement.query(
      By.css('.title')
    );

    const description = fixture.debugElement.query(
      By.css('.description')
    );

    expect(anchor).toBeTruthy();
    expect(anchor.nativeElement.getAttribute('href'))
    .toBe('https://example.com/article');

    expect(anchor.nativeElement.getAttribute('target'))
    .toBe('_blank');

    expect(domain.nativeElement.textContent.trim())
    .toContain('example.com');

    expect(title.nativeElement.textContent.trim())
    .toBe('Example article');

    expect(description.nativeElement.textContent.trim())
    .toBe('Example description');
  });

  it('should not render anything when linkPreview is null', () => {
    component.linkPreview = null as any;

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('a.link-wrapper'))
    ).toBeNull();
  });

  it('should render the favicon when faviconUrl is available', () => {
    component.linkPreview.faviconUrl =
      'https://example.com/favicon.ico';

    fixture.detectChanges();

    const favicon = fixture.debugElement.query(
      By.css('img.favicon')
    );

    const fallbackIcon = fixture.debugElement.query(
      By.css('i.link-icon')
    );

    expect(favicon).toBeTruthy();
    expect(favicon.nativeElement.getAttribute('src'))
    .toBe('https://example.com/favicon.ico');

    expect(favicon.nativeElement.getAttribute('alt'))
    .toBe('example.com favicon');

    expect(fallbackIcon).toBeNull();
  });

  it('should render the fallback icon when faviconUrl is unavailable', () => {
    component.linkPreview.faviconUrl = null as any;

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('img.favicon'))
    ).toBeNull();

    expect(
      fixture.debugElement.query(By.css('i.link-icon'))
    ).toBeTruthy();
  });

  it('should render the preview image when imageUrl is available', () => {
    component.linkPreview.imageUrl =
      'https://example.com/preview.png';

    fixture.detectChanges();

    const image = fixture.debugElement.query(
      By.css('img.preview-img')
    );

    expect(image).toBeTruthy();
    expect(image.nativeElement.getAttribute('src'))
    .toBe('https://example.com/preview.png');

    expect(image.nativeElement.getAttribute('alt'))
    .toBe('example.com preview image');
  });

  it('should not render the preview image when imageUrl is unavailable', () => {
    component.linkPreview.imageUrl = null as any;

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('img.preview-img'))
    ).toBeNull();
  });

  it('should show the block button when the user is the post author', () => {
    component.userIsPostAuthor = true;

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('.block-button'))
    ).toBeTruthy();
  });

  it('should hide the block button when the user is not the post author', () => {
    component.userIsPostAuthor = false;

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('.block-button'))
    ).toBeNull();
  });

  it('should block an unsaved preview without deleting it', () => {
    component.linkPreview.id = null as any;

    const event = jasmine.createSpyObj<Event>(
      'Event',
      ['stopPropagation', 'preventDefault']
    );

    component.blockLinkPreview(event);

    expect(event.stopPropagation).toHaveBeenCalledTimes(1);
    expect(event.preventDefault).toHaveBeenCalledTimes(1);
    expect(component.linkPreview.blocked).toBeTrue();
    expect(linkPreviewServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should block and delete a saved preview', () => {
    component.linkPreview.id = 42;

    const event = jasmine.createSpyObj<Event>(
      'Event',
      ['stopPropagation', 'preventDefault']
    );

    component.blockLinkPreview(event);

    expect(event.stopPropagation).toHaveBeenCalledTimes(1);
    expect(event.preventDefault).toHaveBeenCalledTimes(1);
    expect(component.linkPreview.blocked).toBeTrue();

    expect(linkPreviewServiceSpy.delete)
    .toHaveBeenCalledOnceWith(42);
  });

  it('should block the preview when the block button is clicked', () => {
    component.linkPreview.id = 99;

    fixture.detectChanges();

    const event = jasmine.createSpyObj<Event>(
      'Event',
      ['stopPropagation', 'preventDefault']
    );

    const blockButton = fixture.debugElement.query(
      By.css('.block-button')
    );

    blockButton.triggerEventHandler('click', event);
    fixture.detectChanges();

    expect(event.stopPropagation).toHaveBeenCalledTimes(1);
    expect(event.preventDefault).toHaveBeenCalledTimes(1);
    expect(component.linkPreview.blocked).toBeTrue();

    expect(linkPreviewServiceSpy.delete)
    .toHaveBeenCalledOnceWith(99);

    expect(
      fixture.debugElement.query(By.css('.link-preview'))
    ).toBeNull();
  });

  it('should not render an already blocked preview', () => {
    component.linkPreview.blocked = true;

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('a.link-wrapper'))
    ).toBeTruthy();

    expect(
      fixture.debugElement.query(By.css('.link-preview'))
    ).toBeNull();
  });
});

function createLinkPreview(): LinkPreview {
  return {
    id: 1,
    url: 'https://example.com/article',
    domain: 'example.com',
    title: 'Example article',
    description: 'Example description',
    faviconUrl: 'https://example.com/favicon.ico',
    imageUrl: 'https://example.com/preview.png',
    blocked: false
  } as LinkPreview;
}
