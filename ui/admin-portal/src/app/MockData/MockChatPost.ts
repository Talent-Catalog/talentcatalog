import {ChatPost} from "../model/chat";
import {MockUser} from "./MockUser";
import {Reaction} from "../model/reaction";
import {MockReaction} from "./MockReaction";

export class MockChatPost implements ChatPost {
  content: string = "Sample content";
  createdBy = new MockUser();
  createdDate: Date = new Date("2024-05-01");
  id: number = 1;
  jobChat: any = {}; // Add mock data for JobChat if available
  updatedBy= new MockUser();
  updatedDate: Date = new Date("2024-05-01");
  reactions?: Reaction[] = [new MockReaction()];
}
