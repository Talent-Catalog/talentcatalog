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

/**
 * On-demand, LLM-generated explanation of how a candidate's job experience relates to the
 * description of a Matching. Mirrors the server's CandidateMatchExplanation response DTO.
 */
export interface CandidateMatchExplanation {
  /**
   * When the explanation was generated (ISO-8601 timestamp), as reported by the explanation
   * service. Absent/null for explanations persisted before this was recorded.
   */
  generatedAt?: string;
  /**
   * Name of the LLM model that generated the explanation. Absent/null for explanations persisted
   * before this was recorded.
   */
  modelName?: string;
  summary: string;
  experienceExplanations: ExperienceMatchExplanation[];
  limitations: string[];
}

/** Explanation of how a single candidate job experience relates to the Matching's description. */
export interface ExperienceMatchExplanation {
  experienceId: number;
  /**
   * Job title of the experience, as returned with the explanation. Absent/null for explanations
   * persisted before this was recorded.
   */
  jobTitle?: string;
  explanation: string;
}

/**
 * Request for the explanation of how a candidate relates to the description of a Matching. The
 * candidate is identified separately (by path/method parameter), not by this request body. The
 * server obtains the description from the Matching itself.
 */
export interface CandidateMatchExplanationRequest {
  /** ID of the Matching whose description the candidate is compared against. */
  matchingId: number;
  /**
   * True to always generate a new explanation, replacing any cached one. Otherwise (the default)
   * a cached explanation is returned if it is still current - ie the Matching's description
   * hasn't changed since it was generated.
   */
  regenerate?: boolean;
}
