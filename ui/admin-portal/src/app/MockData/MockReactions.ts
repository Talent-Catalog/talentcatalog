import {Reaction} from "../model/reaction";
import {DisplayUser} from "../model/user";

export class MockReactions implements Reaction {
  id: number;
  emoji: string;
  users: DisplayUser[];

  constructor(id: number, emoji: string, users: DisplayUser[]) {
    this.id = id;
    this.emoji = emoji;
    this.users = users;
  }
}

// Define mock data
export const MOCK_REACTIONS: MockReactions[] = [
  new MockReactions(1, '😊', [{ id: 1, displayName: 'User1' }, { id: 2, displayName: 'User2' }]),
  new MockReactions(2, '👍', [{ id: 3, displayName: 'User3' }]),
  new MockReactions(3, '❤️', [{ id: 4, displayName: 'User4' }, { id: 5, displayName: 'User5' }, { id: 6, displayName: 'User6' }])
];
