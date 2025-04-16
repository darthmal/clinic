// Matches com.clinicapp.backend.model.chat.ChatMessage

export interface ChatMessage {
  type: 'CHAT' | 'JOIN' | 'LEAVE'; // Match backend enum
  content: string;
  sender: string; // Username
  recipient?: string; // Username (for private messages)
  timestamp?: string; // ISO string format
}