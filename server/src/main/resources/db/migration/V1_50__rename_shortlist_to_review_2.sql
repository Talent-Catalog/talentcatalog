/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

alter table candidate_review_item rename constraint candidate_shortlist_item_pkey to candidate_review_item_pkey;
alter table candidate_review_item rename constraint candidate_shortlist_item_candidate_id_fkey to candidate_review_item_candidate_id_fkey;
alter table candidate_review_item rename constraint candidate_shortlist_item_created_by_fkey to candidate_review_item_created_by_fkey;
alter table candidate_review_item rename constraint candidate_shortlist_item_saved_search_id_fkey to candidate_review_item_saved_search_id_fkey;
alter table candidate_review_item rename constraint candidate_shortlist_item_updated_by_fkey to candidate_review_item_updated_by_fkey;
