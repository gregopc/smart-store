import { Component, inject } from '@angular/core';
import { AiChatService } from '../../../core/services/ai-chat.service';


@Component({
  selector: 'app-chat',
  imports: [],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css',
})
export class ChatComponent {
  aiChatService = inject(AiChatService);
}
