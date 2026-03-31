/*
 * Copyright (c) 2026 Talent Catalog.
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

/**
 * Enumeration of service providers.
 */
export enum ServiceProvider {
  LINKEDIN = "LINKEDIN",
  DUOLINGO = "DUOLINGO",
  REFERENCE = "REFERENCE",
  UNHCR = "UNHCR"
}

/**
 * Enumeration of service codes representing different types of services.
 */
enum ServiceCode {
  TEST_PROCTORED = "TEST_PROCTORED",
  TEST_NON_PROCTORED = "TEST_NON_PROCTORED",
  PREMIUM_MEMBERSHIP = "PREMIUM_MEMBERSHIP",
  VOUCHER = "VOUCHER",
  HELP_SITE_LINK = "HELP_SITE_LINK",
}

/**
 * Status of a resource (e.g., a coupon code).
 */
export enum ResourceStatus {
  AVAILABLE = "AVAILABLE",   // unallocated, ready to use
  RESERVED = "RESERVED",     // allocated to a candidate, not yet sent
  SENT = "SENT",             // sent/communicated to candidate
  REDEEMED = "REDEEMED",     // used by candidate
  EXPIRED = "EXPIRED",       // no longer valid
  DISABLED = "DISABLED",     // manually disabled/blocked
}

/**
 * Status of a service assignment.
 */
export enum AssignmentStatus {
  ASSIGNED = "ASSIGNED",     // assigned to candidate
  REDEEMED = "REDEEMED",     // used by candidate
  EXPIRED = "EXPIRED",       // no longer valid
  DISABLED = "DISABLED",     // manually disabled/blocked
  REASSIGNED = "REASSIGNED", // a new assignment has been made replacing this one
}

export interface UpdateServiceResourceStatusRequest {
  resourceCode: string,
  status: ResourceStatus
}

export interface IssueReportRequest {
  assignment: ServiceAssignment;
  issueComment: string;
}

/**
 * Represents the assignment of a service to a candidate.
 */
export interface ServiceAssignment {
  id: number;
  provider: ServiceProvider;
  serviceCode: ServiceCode;
  resource: ServiceResource; // e.g., coupon code
  candidateId: number;
  actorId: number; // who assigned it
  status: AssignmentStatus; // ASSIGNED, REDEEMED, EXPIRED, REASSIGNED
  assignedAt: string; // ISO 8601 datetime
}

/**
 * Represents a service resource assigned to a candidate.
 */
export interface ServiceResource {
  id: number;
  provider: ServiceProvider;
  serviceCode: ServiceCode;
  resourceCode: string;
  status: ResourceStatus;
  sentAt: string; // ISO 8601 datetime
  expiresAt: string; // ISO 8601 datetime
}
