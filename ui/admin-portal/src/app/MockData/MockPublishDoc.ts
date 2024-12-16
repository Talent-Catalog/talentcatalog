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


// Mock data for PublishedDocColumnProps
import {
  PublishedDocColumnConfig,
  PublishedDocColumnContent, PublishedDocColumnDef,
  PublishedDocColumnProps,
  PublishedDocFieldSource,
  PublishedDocValueSource
} from "../model/saved-list";
import {PublishedDocColumnType, PublishedDocColumnWidth} from "../model/base";

const mockColumnProps: PublishedDocColumnProps = {
  header: 'Custom Header',
  constant: 'Constant Value'
};

// Mock data for PublishedDocValueSource
const mockValueSource: PublishedDocValueSource = new PublishedDocFieldSource('mockField');

// Mock data for PublishedDocColumnContent
const mockColumnContent: PublishedDocColumnContent = {
  link: mockValueSource,
  value: mockValueSource
};

// Mock data for PublishedDocColumnDef
const mockColumnDef: PublishedDocColumnDef = {
  key: 'mockKey',
  name: 'Mock Name',
  header: 'Mock Header',
  type: PublishedDocColumnType.DisplayOnly,
  width: PublishedDocColumnWidth.Medium,
  content: mockColumnContent
};

// Mock data for PublishedDocColumnConfig
const mockPublishedDocColumnConfig: PublishedDocColumnConfig = {
  columnProps: mockColumnProps,
  columnDef: mockColumnDef
};

// Array of mock PublishedDocColumnConfig
const mockPublishedDocColumnConfigs: PublishedDocColumnConfig[] = [
  mockPublishedDocColumnConfig,
  {
    columnProps: {
      header: 'Another Custom Header',
      constant: 'Another Constant Value'
    },
    columnDef: {
      key: 'anotherMockKey',
      name: 'Another Mock Name',
      header: 'Another Mock Header',
      type: PublishedDocColumnType.DisplayOnly,
      width: PublishedDocColumnWidth.Medium,
      content: {
        link: new PublishedDocFieldSource('anotherMockField'),
        value: new PublishedDocFieldSource('anotherMockField')
      }
    }
  }
];

export { mockPublishedDocColumnConfig, mockPublishedDocColumnConfigs };
