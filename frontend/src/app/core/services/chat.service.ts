import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from '../../shared/enviroments/environments';
import { firstValueFrom } from 'rxjs';
import { ChatSuggestionResponse, ProductDTO } from './dto/chat-response';

export interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly API_URL = `${environment.apiUrl}/assistant/suggestion`;
  private readonly http = inject(HttpClient);

  suggestedProducts = signal<ProductDTO[]>([]);
  messages = signal<Message[]>([]);
  isLoading = signal(false);
  isOpen = signal(false);

  toggleChat() { this.isOpen.update(v => !v); }
  openChat() { this.isOpen.set(true); }
  closeChat() { this.isOpen.set(false); }
  clearMessages() { this.messages.set([]); }

  async getSuggestionFromMessage(userText: string): Promise<void> {
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
      const messages = this.messages().map(m => ({ role: m.role, content: m.content }));

      const body = {
        messages
      };
      const data = await firstValueFrom(this.http.post<ChatSuggestionResponse>(this.API_URL, body));

      const text = data.suggestion.reply
        || data.message
        || 'Desculpe, não consegui gerar sua resposta, tente novamente em outro momento.';

      this.messages.update(msgs => [...msgs, {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: text,
        timestamp: new Date(),
      }]);

      this.suggestedProducts.set(data.suggestion?.suggestedProducts ?? []);
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
