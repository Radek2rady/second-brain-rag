import { useState, useEffect } from 'react';
import type { ChatMessage } from '../types';

export function useChat(token: string | null) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [conversations, setConversations] = useState<string[]>([]);

  const fetchConversations = async () => {
    if (!token) return;
    try {
      const res = await fetch('http://localhost:8080/api/documents/conversations', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) setConversations(await res.json());
    } catch (e) { console.error(e); }
  };

  const loadConversation = async (id: string, updateState = false, onLoaded?: () => void) => {
    if (updateState) {
      setConversationId(id);
      localStorage.setItem('rag_conversation_id', id);
      if (onLoaded) onLoaded();
    }
    try {
      const res = await fetch(`http://localhost:8080/api/documents/chat/history?conversationId=${id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const history = await res.json();
        setMessages(history.map((m: any) => ({
          ...m,
          source: 'LOCAL',
          references: [],
        })));
      }
    } catch (e) { console.error(e); }
  };

  useEffect(() => {
    if (token) {
      const savedId = localStorage.getItem('rag_conversation_id');
      if (savedId) {
        setConversationId(savedId);
        loadConversation(savedId);
      }
      fetchConversations();
    }
  }, [token]);

  const handleNewChat = (onNewChat?: () => void) => {
    localStorage.removeItem('rag_conversation_id');
    setConversationId(null);
    setMessages([]);
    if (onNewChat) onNewChat();
  };

  const handleClearHistory = (onClear?: () => void) => {
    if (confirm('Are you sure you want to clear local history?')) {
      handleNewChat(onClear);
    }
  };

  const sendMessage = async (input: string) => {
    if (!input.trim() || isLoading || !token) return;

    const userMessage = input.trim();
    setMessages(prev => [...prev, { role: 'user', content: userMessage }]);
    setIsLoading(true);

    try {
      const params = new URLSearchParams({ query: userMessage });
      if (conversationId) params.append('conversationId', conversationId);

      const response = await fetch(`http://localhost:8080/api/documents/chat?${params.toString()}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      if (!response.ok) throw new Error('Network response was not ok');
      const data = await response.json();

      if (data.conversationId && data.conversationId !== conversationId) {
        setConversationId(data.conversationId);
        localStorage.setItem('rag_conversation_id', data.conversationId);
        if (!conversations.includes(data.conversationId)) {
          setConversations(prev => [data.conversationId, ...prev]);
        }
      }

      setMessages(prev => [...prev, {
        role: 'assistant',
        content: data.answer,
        source: data.source || 'LOCAL',
        references: data.references || [],
      }]);
    } catch (error) {
      console.error(error);
      setMessages(prev => [
        ...prev,
        { role: 'assistant', content: 'I am sorry, an error occurred while communicating with the server.' }
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  return {
    messages,
    isLoading,
    conversationId,
    conversations,
    sendMessage,
    loadConversation,
    handleNewChat,
    handleClearHistory,
  };
}
