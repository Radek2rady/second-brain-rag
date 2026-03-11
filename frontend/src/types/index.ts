export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  source?: 'LOCAL' | 'WEB' | 'HYBRID';
  references?: string[];
}

export interface VectorDocument {
  id: string;
  content: string;
  metadata: Record<string, string>;
}
