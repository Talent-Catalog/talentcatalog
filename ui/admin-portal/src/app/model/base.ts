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

import {Role, User} from './user';
import {ExportColumn} from "./saved-list";
import {OpportunityIds} from "./opportunity";

export interface HasName {
  name?: string;
}

export enum CandidateSourceType {
  SavedList,
  SavedSearch
}

export enum Progress {
  NotStarted,
  Started,
  Finished
}

/**
 * Published columns can for display purposes only, or they can allow different kinds of data to be
 * entered in them (eg by employers) which we can process, interpreting the data depending on its
 * type.
 * This entered data may get imported back into the Talent Catalog data base or into Salesforce.
 */
export enum PublishedDocColumnType {
  DisplayOnly,
  EmployerCandidateNotes,
  EmployerCandidateDecision,
  YesNoDropdown
}

/**
 * Published doc columns can be configured in three sizes based on their width.
 * <p/>
 * The width can be used to determine other automated formatting - eg alignment.
 */
export enum PublishedDocColumnWidth {
  /**
   * Narrow columns are good for small amounts of data - eg a candidate number, or a status.
   * <p/>
   * Automated formatting may choose to center align the values in narrow columns.
   */
  Narrow,

  /**
   * This is the default column width and does not need to be specified. It can be used for
   * candidate names, for example.
   * <p/>
   * It doesn't trigger any automated formatting. The defaults for the template from which a doc
   * is created will be used.
   */
  Medium,

  /**
   * Wide columns are good for holding descriptive text - eg candidate descriptions or
   * employer feedback.
   * <p/>
   * Automated formatting will typically choose to left justify the text.
   */
  Wide
}

export enum ReviewStatus {
  unverified,
  verified,
  rejected
}

/**
 * This restricts the data displayed in our browse tabs.
 * For example, if a tab is showing all my lists or all my searches - this would be "all".
 * Or if a tab is showing all searches of a given type (eg Profession, Job, Other), this would
 * be "type"
 */
export enum SearchBy {
  type,
  all,
  mine,
  sharedWithMe,
  watched,
  externalLink,
  registeredJob
}

/**
 * Different kinds of searches for both Job Opportunities and Candidate Opportunities
 */
export enum SearchOppsBy {
  all,
  mineAsSourcePartner,
  mineAsJobCreator,
  starredByMe,
  live
}

/**
 * Defines what TBB Salesforce url should look like.
 * <p/>
 * Basically it starts with TBB's Salesforce url. It also contains a
 * Salesforce record ID which is a string 15 or more of "word" characters
 * preceeded by a record type delimited by "/".
 * eg /Opportunity/...id...
 * Any validation method needs to use BOTH the SF sandbox and prod patterns below, combining them as OR options using a pipe: Validators.pattern(`${salesforceUrlPattern}|${salesforceSandboxUrlPattern}`)
 */
export const salesforceUrlPattern: string =
  'https://talentbeyondboundaries.lightning.force.com/lightning/r/' +
  '[\\w]+/[\\w]{15,}[^\\w]?.*';

export const salesforceSandboxUrlPattern: string =
  'https://talentbeyondboundaries--sfstaging.sandbox.lightning.force.com/lightning/r/' +
    '[\\w]+/[\\w]{15,}[^\\w]?.*';

export const salesforceUrlRegExp: RegExp = new RegExp(salesforceUrlPattern);

/**
 * Defines the start of a linkedIn profile URL.
 * Defined here in case the link structure changes so only needs changing in one place.
 */
export const linkedInUrl: string = 'https://www.linkedin.com/in/';

export const defaultReviewStatusFilter: string[] = [
  ReviewStatus[ReviewStatus.rejected]
];

export interface HasId {
  id?: number;
}

export function indexOfHasId(id: number, hasIds: HasId[]): number {
  for (let i = 0; i < hasIds.length; i++) {
    if (hasIds[i].id === id) {
      return i;
    }
  }
  return -1;
}

export function findHasId(id: number, hasIds: HasId[]): HasId {
  const idx = indexOfHasId(id, hasIds);
  return idx < 0 ? null : hasIds[idx];
}

export interface Auditable extends HasId {
  createdBy?: User;
  createdDate?: Date;
  updatedBy?: User
  updatedDate?: Date;
}

export interface CandidateSource extends Auditable {
  name: string;
  description?: string;
  displayedFieldsLong?: string[];
  displayedFieldsShort?: string[];
  exportColumns?: ExportColumn[];
  fixed: boolean;
  global: boolean;
  sfJobOpp?: OpportunityIds;
  users?: User[];
  watcherUserIds?: number[];
  dtoType?: DtoType;
}


export interface HasJobRelatedLinks {
  sfJoblink: string;
  listlink?: string;
  fileJdLink?: string;
  fileJdName?: string;
  fileJoiLink?: string;
  fileJoiName?: string;
  folderlink?: string;
  folderjdlink?: string;
  jobId?: number;
}

export interface UpdateEmployerOpportunityRequest extends HasJobRelatedLinks {
}

export interface PostJobToSlackRequest extends HasJobRelatedLinks {
  jobName?: string;
}
export interface PostJobToSlackResponse {
  slackChannelUrl: string;
}

export enum DtoType {
  MINIMAL = 'MINIMAL',
  PREVIEW = 'PREVIEW',
  FULL = 'FULL',
  EXTENDED = 'EXTENDED'
}

export class IdsRequest {
  dtoType?: DtoType;
  ids: number[];
}

export class PagedSearchRequest {
  dtoType?: DtoType;
  pageSize?: number;
  pageNumber?: number;
  sortFields?: string[];
  sortDirection?: string;
}

export class PagedFilteredSearchRequest extends PagedSearchRequest {
  keyword?: string;
  status?: string;
}

export class SearchPartnerRequest extends PagedFilteredSearchRequest {
  contextJobId?: number;
  jobCreator?: boolean;
  sourcePartner?: boolean;
}

export class SearchUserRequest extends PagedFilteredSearchRequest {
  partnerId?: number;
  role?: Role;
}

export class SearchTaskRequest extends PagedFilteredSearchRequest {}

export class SearchCandidateSourcesRequest extends PagedSearchRequest {
  keyword?: string;
  fixed?: boolean;
  global?: boolean;
  owned?: boolean;
  shared?: boolean;
  watched?: boolean;
}

export class UpdateCandidateContextNoteRequest {
  candidateId: number;
  contextNote: string;
}

export class UpdateCandidateSourceDescriptionRequest {
  description: string;
}

export class UpdateDisplayedFieldPathsRequest {
  displayedFieldsLong?: string[];
  displayedFieldsShort?: string[];
}

export class LoginRequest {
  username: string;
  password: string;
  reCaptchaV3Token: string;

  /**
   * Time based One Time Password (TOTP) used for multi factor authentication
   */
  totpToken: string;
}

// Regex breakdown (rules and test cases from https://en.wikipedia.org/wiki/Email_address):
// - negative lookahead to ensure no consecutive '.' and no '.' at the end of the prefix
// - prefix begins with upper or lowercase Latin letter, digits 0-9 or printable character
// (!#$%&'*+-/=?^_`{|}~)
// - prefix continues with any of above or '.'
// - domain preceded by '@' consists of upper or lowercase Latin letter(s), digits 0-9 or hyphen
// (which must not be at start or end)
// 0 or more subdomains and/or a TLD preceded by '.', otherwise with same rules as domain
//
// These rules enable subaddressing (e.g. sam+test@tbb.org) and some other aspects that aren't
// always allowed by mainstream email services. They do not allow for different rules applied within
// quotation marks or the use of IP address literals, which sometimes are.
//
// NB: This regex is replicated in ui/candidate-portal/src/app/model/base.ts — any changes needed
// here will also need to be replicated there!

export const EMAIL_REGEX: string =
  '(?!.*[@.]{2})[a-zA-Z0-9!#$%&\'*+-/=?^_`{|}~]+[a-zA-Z0-9.!#$%&\'*+-/=?^_`{|}~]*@(?!-)[a-zA-Z0-9-]+(?<!-)(\\.(?!-)[a-zA-Z0-9-]+(?<!-))*$';

/**
 * URL validation, also accepting 'mailto:' links, from
 * <a href="https://regex101.com/library/4hNOPu">regex101</a>
 */
export const URL_REGEX: string =
  '(mailto:[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$)|(((?:https?)|(?:ftp)):\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})'

export enum Status {
  active = "active",
  inactive = "inactive",
  deleted = "deleted"
}

/**
 * Interface for sharing link-formatted text properties.
 * See {@link CreateUpdatePostComponent}
 */
export interface Link {
  /**
   * Display text
   */
  placeholder: string,
  /**
   * Navigate to
   */
  url: string
}

/**
 * Interface for sharing text editor selection properties.
 * See {@link CreateUpdatePostComponent}
 */
export interface EditorSelection {
  /**
   * Index position of user selection
   */
  userSelectionIndex: number,
  /**
   * No. of characters included after index in user selection
   */
  userSelectionLength: number,
  /**
   * Placeholder for link (user selection if new link, current placeholder if existing link)
   */
  placeholder?: string,
  /**
   * Index position of beginning of link
   */
  linkIndex?: number,
  /**
   * No. of characters from link index in entire link-formatted text
   */
  linkLength?: number,
  /**
   * URL if selection is a link
   */
  linkUrl?: string
}

export interface FetchCandidatesWithChatRequest extends PagedFilteredSearchRequest {
  unreadOnly: boolean;
}
