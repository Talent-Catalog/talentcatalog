/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {PreviewLinkComponent} from './preview-link.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LinkPreview} from '../../../model/link-preview';  // Assuming LinkPreview is the model

describe('PreviewLinkComponent', () => {
  let component: PreviewLinkComponent;
  let fixture: ComponentFixture<PreviewLinkComponent>;

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
    await TestBed.configureTestingModule({
      declarations: [PreviewLinkComponent],
      imports: [HttpClientTestingModule],
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
});
