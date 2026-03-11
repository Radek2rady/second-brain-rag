import { useState, useRef, useEffect } from 'react';
import { Send, Paperclip, Loader2, Bot } from 'lucide-react';
import type { ChatMessage } from '../types';
import { MessageBubble } from './MessageBubble';

interface ChatWindowProps {
  messages: ChatMessage[];
  isLoading: boolean;
  sendMessage: (input: string) => void;
  isUploading: boolean;
  uploadStatus: string | null;
  fileInputRef: React.RefObject<HTMLInputElement>;
}

export function ChatWindow({
  messages, isLoading, sendMessage, isUploading, uploadStatus, fileInputRef
}: ChatWindowProps) {
  const [input, setInput] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading, uploadStatus]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const currentInput = input;
    setInput('');
    sendMessage(currentInput);
    
    setTimeout(() => {
      inputRef.current?.focus();
    }, 100);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <main className="flex-1 flex flex-col min-w-0 h-full overflow-hidden bg-slate-950">
      {/* Chat Messages */}
      <div className="flex-1 overflow-y-auto p-4 md:p-6 scroll-smooth">
        <div className="max-w-3xl mx-auto space-y-6">
          {messages.length === 0 ? (
            <div className="flex flex-col items-center justify-center min-h-[50vh] text-slate-400 space-y-4 animate-[fadeIn_0.5s_ease-out]">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-500/20 to-indigo-500/20 flex items-center justify-center border border-blue-500/30 shadow-lg shadow-blue-500/10">
                <Bot className="w-8 h-8 text-blue-400" />
              </div>
              <div className="text-center">
                <h3 className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-indigo-400 mb-2">How can I help you today?</h3>
                <p className="text-sm text-slate-500 max-w-sm">I can answer questions based on your uploaded documents. Type a message or upload a new file below.</p>
              </div>
            </div>
          ) : (
            <>
              {messages.map((msg, index) => (
                <div key={index} className="animate-[fadeIn_0.3s_ease-out]">
                  <MessageBubble msg={msg} index={index} />
                </div>
              ))}
              
              {/* Status and Loading Indicators */}
              {(isLoading || uploadStatus) && (
                <div className="flex gap-4 p-4 rounded-xl animate-[fadeIn_0.3s_ease-out]">
                  <div className="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center flex-shrink-0 mt-1 shadow-sm">
                    <Bot className="w-5 h-5 text-white" />
                  </div>
                  <div className="flex-1 min-w-0 py-1.5 border-l-2 border-blue-500/30 pl-3 ml-1">
                    <div className="flex items-center gap-3">
                      <div className="flex gap-1">
                        <span className="w-1.5 h-1.5 bg-blue-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></span>
                        <span className="w-1.5 h-1.5 bg-blue-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></span>
                        <span className="w-1.5 h-1.5 bg-blue-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></span>
                      </div>
                      {uploadStatus ? (
                         <span className="text-sm text-slate-400 font-medium">{uploadStatus}</span>
                      ) : (
                         <span className="text-sm text-slate-400 font-medium">Second Brain is searching documents and thinking...</span>
                      )}
                    </div>
                  </div>
                </div>
              )}
            </>
          )}
          <div ref={messagesEndRef} className="h-4" />
        </div>
      </div>

      {/* Input Area */}
      <footer className="p-4 md:p-6 bg-slate-950 border-t border-slate-800">
        <div className="max-w-3xl mx-auto">
          <form 
            onSubmit={handleSubmit}
            className="relative flex items-end bg-slate-900 rounded-2xl border border-slate-700 focus-within:border-blue-500/50 focus-within:ring-1 focus-within:ring-blue-500/50 transition-all shadow-lg"
          >
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              disabled={isUploading}
              className="absolute left-2 bottom-2 p-2 text-slate-400 hover:text-blue-400 hover:bg-slate-800 rounded-xl transition-colors disabled:opacity-50"
              title="Upload File (.txt, .md)"
            >
              {isUploading ? (
                <Loader2 className="w-5 h-5 animate-spin text-blue-400" />
              ) : (
                <Paperclip className="w-5 h-5" />
              )}
            </button>

            <textarea
              ref={inputRef}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Message Second Brain..."
              className="w-full max-h-[200px] py-3.5 pl-12 pr-12 bg-transparent border-none focus:outline-none focus:ring-0 text-[15px] resize-none text-slate-200 placeholder-slate-400 disabled:opacity-50"
              rows={1}
              disabled={isLoading}
              style={{ minHeight: '52px' }}
            />
            <div className="absolute right-2 bottom-2 bg-slate-800 rounded-full">
              <button
                type="submit"
                disabled={!input.trim() || isLoading}
                className="p-2 bg-blue-600 text-white rounded-full hover:bg-blue-500 transition-colors disabled:opacity-30 disabled:bg-slate-600 disabled:cursor-not-allowed shadow-sm"
              >
                <Send className="w-4 h-4 translate-x-[-1px] translate-y-[1px]" />
              </button>
            </div>
          </form>
          <div className="text-center mt-2 text-xs text-slate-500 flex items-center justify-center gap-1">
            Second Brain RAG can make mistakes. Verify important information.
          </div>
        </div>
      </footer>
    </main>
  );
}
