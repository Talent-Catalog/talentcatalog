import {User} from "./user";
import {SavedSearch} from "./saved-search";

export interface CandidateShortlistItem {
  id: number;
  savedSearch: SavedSearch;
  shortlistStatus: string;
  comment: string;
  createdBy: User;
  createdDate: number;
  updatedBy: User
  updatedDate: number;
}
