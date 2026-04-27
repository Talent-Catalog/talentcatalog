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

// Populate the constant object with predefined values
import {SavedSearchTypeInfo} from "../services/saved-search.service";
import {SavedSearchSubtype} from "../model/saved-search";
// Array of SavedSearchTypeInfo
const MOCK_SAVED_SEARCH_TYPE_INFO: SavedSearchTypeInfo[] = [
  {
    title: 'Profession',
    categories: [
      { savedSearchSubtype: SavedSearchSubtype.business, title: 'Business' },
      { savedSearchSubtype: SavedSearchSubtype.agriculture, title: 'Agriculture' },
      { savedSearchSubtype: SavedSearchSubtype.healthcare, title: 'Healthcare' },
      { savedSearchSubtype: SavedSearchSubtype.engineering, title: 'Engineering' },
      { savedSearchSubtype: SavedSearchSubtype.food, title: 'Food' },
      { savedSearchSubtype: SavedSearchSubtype.education, title: 'Education' },
      { savedSearchSubtype: SavedSearchSubtype.labourer, title: 'Labourer' },
      { savedSearchSubtype: SavedSearchSubtype.trade, title: 'Trade' },
      { savedSearchSubtype: SavedSearchSubtype.arts, title: 'Arts' },
      { savedSearchSubtype: SavedSearchSubtype.it, title: 'IT' },
      { savedSearchSubtype: SavedSearchSubtype.social, title: 'Social' },
      { savedSearchSubtype: SavedSearchSubtype.science, title: 'Science' },
      { savedSearchSubtype: SavedSearchSubtype.law, title: 'Law' },
      { savedSearchSubtype: SavedSearchSubtype.other, title: 'Other' }
    ]
  },
  {
    title: 'Job',
    categories: [
      { savedSearchSubtype: SavedSearchSubtype.au, title: 'Australia' },
      { savedSearchSubtype: SavedSearchSubtype.ca, title: 'Canada' },
      { savedSearchSubtype: SavedSearchSubtype.uk, title: 'United Kingdom' }
    ]
  },
  {
    title: 'Business',
    categories: [
      { savedSearchSubtype: SavedSearchSubtype.au, title: 'Australia' },
      { savedSearchSubtype: SavedSearchSubtype.ca, title: 'Canada' },
      { savedSearchSubtype: SavedSearchSubtype.uk, title: 'United Kingdom' }
    ]
  }
  ,{
    title: 'Education',
    categories: [
      { savedSearchSubtype: SavedSearchSubtype.au, title: 'Australia' },
      { savedSearchSubtype: SavedSearchSubtype.ca, title: 'Canada' },
      { savedSearchSubtype: SavedSearchSubtype.uk, title: 'United Kingdom' }
    ]
  }
];

export default MOCK_SAVED_SEARCH_TYPE_INFO;
