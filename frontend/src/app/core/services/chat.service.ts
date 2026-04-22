import { Injectable, signal } from '@angular/core';
import { environment } from '../../shared/enviroments/environments';

export interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly API_URL = `${environment.apiUrl}/products`;

  messages = signal<Message[]>([]);
  isLoading = signal(false);
  isOpen = signal(false);

  toggleChat() { this.isOpen.update(v => !v); }
  openChat() { this.isOpen.set(true); }
  closeChat() { this.isOpen.set(false); }
  clearMessages() { this.messages.set([]); }

  async sendMessage(userText: string): Promise<void> {
    if (!userText.trim() || this.isLoading()) return;

    const userMsg: Message = {
      id: crypto.randomUUID(),
      role: 'user',
      content: userText.trim(),
      timestamp: new Date(),
    };
    this.messages.update(msgs => [...msgs, userMsg]);
    this.isLoading.set(true);

    try {
      const history = this.messages().map(m => ({ role: m.role, content: m.content }));
      const response = await fetch('https://api.anthropic.com/v1/messages', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: 'claude-sonnet-4-20250514',
          max_tokens: 1000,
          system: 'You are a friendly, helpful AI assistant embedded in a modern web application. Be concise, warm, and genuinely useful.',
          messages: history,
        }),
      });

      const data = await response.json();
      const text = data.content?.map((b: any) => b.text || '').join('') || 'Sorry, I could not generate a response.';

      this.messages.update(msgs => [...msgs, {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: text,
        timestamp: new Date(),
      }]);
    } catch {
      this.messages.update(msgs => [...msgs, {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: 'Oops — something went wrong. Please try again.',
        timestamp: new Date(),
      }]);
    } finally {
      this.isLoading.set(false);
    }
  }
}
