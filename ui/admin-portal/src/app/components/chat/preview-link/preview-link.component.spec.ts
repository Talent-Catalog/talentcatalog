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
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {PreviewLinkComponent} from './preview-link.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LinkPreviewService} from '../../../services/link-preview.service';
import {of} from 'rxjs';
import {LinkPreview} from '../../../model/link-preview';

describe('PreviewLinkComponent', () => {
  let component: PreviewLinkComponent;
  let fixture: ComponentFixture<PreviewLinkComponent>;
  let linkPreviewService: jasmine.SpyObj<LinkPreviewService>;

  // Mock LinkPreview data
  const mockLinkPreview: LinkPreview = {
    id: 1,
    url: 'https://example.com',
    blocked: false,
    domain: 'example.com',
    title: 'Example Title',
    description: 'Example description',
    imageUrl: 'https://example.com/image.jpg',
    faviconUrl: 'https://example.com/favicon.ico',
  };

  beforeEach(async () => {
    linkPreviewService = jasmine.createSpyObj<LinkPreviewService>(
      'LinkPreviewService',
      ['delete']
    );
    linkPreviewService.delete.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      declarations: [PreviewLinkComponent],
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: LinkPreviewService,
          useValue: linkPreviewService
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PreviewLinkComponent);
    component = fixture.componentInstance;

    // Assign the mock link preview to the component before running change detection
    component.linkPreview = mockLinkPreview;
    component.userIsPostAuthor = true;  // Mock that the user is the post author

    fixture.detectChanges();  // Run change detection after assigning values
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the link preview', () => {
    const compiled = fixture.nativeElement;
    const link = compiled.querySelector('.link-wrapper');
    expect(link).toBeTruthy();
    expect(link.href).toContain('https://example.com');
  });

  it('should display the block button if user is post author', () => {
    const compiled = fixture.nativeElement;
    const blockButton = compiled.querySelector('.block-button');
    expect(blockButton).toBeTruthy();
  });

  it('should not display block button if user is not post author', () => {
    component.userIsPostAuthor = false;  // Change user role in the test
    fixture.detectChanges();  // Trigger change detection

    const compiled = fixture.nativeElement;
    const blockButton = compiled.querySelector('.block-button');
    expect(blockButton).toBeNull();  // Block button should not be present
  });

  it('should run ngOnInit', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  it('should render link preview details', () => {
    const compiled: HTMLElement = fixture.nativeElement;

    expect(compiled.textContent).toContain(mockLinkPreview.domain);
    expect(compiled.textContent).toContain(mockLinkPreview.title);
    expect(compiled.textContent).toContain(mockLinkPreview.description);

    const favicon = compiled.querySelector(
      '.favicon'
    ) as HTMLImageElement;

    const previewImage = compiled.querySelector(
      '.preview-img'
    ) as HTMLImageElement;

    expect(favicon).toBeTruthy();
    expect(favicon.src).toContain(mockLinkPreview.faviconUrl);
    expect(previewImage).toBeTruthy();
    expect(previewImage.src).toContain(mockLinkPreview.imageUrl);
  });

  it('should render fallback link icon when favicon is missing', () => {
    component.linkPreview = {
      ...mockLinkPreview,
      faviconUrl: null
    };

    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.favicon')
    ).toBeNull();

    expect(
      fixture.nativeElement.querySelector('.link-icon')
    ).toBeTruthy();
  });

  it('should not render preview image when image URL is missing', () => {
    component.linkPreview = {
      ...mockLinkPreview,
      imageUrl: null
    };

    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.preview-img')
    ).toBeNull();
  });

  it('should not render preview content when preview is blocked', () => {
    component.linkPreview = {
      ...mockLinkPreview,
      blocked: true
    };

    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.link-preview')
    ).toBeNull();
  });

  it('should not render anything when linkPreview is absent', () => {
    component.linkPreview = null;

    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.link-wrapper')
    ).toBeNull();
  });

  it('should block preview, stop browser navigation and delete persisted preview', () => {
    const event = jasmine.createSpyObj<Event>(
      'Event',
      ['stopPropagation', 'preventDefault']
    );

    component.linkPreview = {
      ...mockLinkPreview,
      id: 5,
      blocked: false
    };

    component.blockLinkPreview(event);

    expect(event.stopPropagation).toHaveBeenCalledTimes(1);
    expect(event.preventDefault).toHaveBeenCalledTimes(1);
    expect(component.linkPreview.blocked).toBeTrue();
    expect(linkPreviewService.delete).toHaveBeenCalledOnceWith(5);
  });

  it('should block a new preview without calling delete', () => {
    const event = jasmine.createSpyObj<Event>(
      'Event',
      ['stopPropagation', 'preventDefault']
    );

    component.linkPreview = {
      ...mockLinkPreview,
      id: null,
      blocked: false
    };

    component.blockLinkPreview(event);

    expect(event.stopPropagation).toHaveBeenCalledTimes(1);
    expect(event.preventDefault).toHaveBeenCalledTimes(1);
    expect(component.linkPreview.blocked).toBeTrue();
    expect(linkPreviewService.delete).not.toHaveBeenCalled();
  });

});
