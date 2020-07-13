import {User} from "./user";
import {SavedSearch} from "./saved-search";

export interface CandidateReviewStatusItem {
  id: number;
  savedSearch: SavedSearch;
  reviewStatus: string;
  comment: string;
  createdBy: User;
  createdDate: number;
  updatedBy: User
  updatedDate: number;
}
