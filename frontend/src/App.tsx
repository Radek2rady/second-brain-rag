import React, { useState, useRef, useEffect } from 'react';
import { Send, User, Bot, Plus, MessageSquare, Trash2, Menu, X, Database, FileText, Paperclip, Loader2, Globe, HardDrive, AlertTriangle, ExternalLink, Blend, ShieldAlert, LayoutDashboard, Users } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import Login from './Login';
import AuditLogs from './AuditLogs';
import UserList from './UserList';
import AdminOverview from './AdminOverview';

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  source?: 'LOCAL' | 'WEB' | 'HYBRID';
  references?: string[];
}

interface VectorDocument {
  id: string;
  content: string;
  metadata: Record<string, string>;
}

/* ─── Source Badge Component ─── */
function SourceBadge({ source }: { source: 'LOCAL' | 'WEB' | 'HYBRID' }) {
  const config = {
    LOCAL: {
      icon: <HardDrive className="w-3 h-3" />,
      label: 'Source: Your Second Brain',
      bg: 'bg-emerald-500/15',
      border: 'border-emerald-500/30',
      text: 'text-emerald-400',
      dot: 'bg-emerald-400',
    },
    WEB: {
      icon: <Globe className="w-3 h-3" />,
      label: 'Source: Internet',
      bg: 'bg-amber-500/15',
      border: 'border-amber-500/30',
      text: 'text-amber-400',
      dot: 'bg-amber-400',
    },
    HYBRID: {
      icon: <Blend className="w-3 h-3" />,
      label: 'Source: Second Brain + Internet',
      bg: 'bg-blue-500/15',
      border: 'border-blue-500/30',
      text: 'text-blue-400',
      dot: 'bg-blue-400',
    },
  };

  const c = config[source];

  return (
    <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${c.bg} ${c.border} ${c.text} border mb-2 animate-[fadeIn_0.3s_ease-out]`}>
      <span className={`w-1.5 h-1.5 rounded-full ${c.dot} animate-pulse`} />
      {c.icon}
      {c.label}
    </div>
  );
}

/* ─── Hallucination Warning Component ─── */
function HallucinationWarning() {
  return (
    <div className="mt-3 p-3 rounded-xl bg-amber-500/10 border border-amber-500/25 text-amber-300 text-xs leading-relaxed animate-[fadeIn_0.4s_ease-out] flex items-start gap-2">
      <AlertTriangle className="w-4 h-4 flex-shrink-0 mt-0.5 text-amber-400" />
      <div>
        <span className="font-semibold text-amber-400">Warning:</span> This response comes from the internet.
        The information has not been verified against your knowledge base.
        The model may <span className="font-semibold">hallucinate</span> or lack logical reasoning — verify the facts from trusted sources.
      </div>
    </div>
  );
}

/* ─── References List Component ─── */
function ReferencesList({ references }: { references: string[] }) {
  if (references.length === 0) return null;

  return (
    <div className="mt-3 pt-3 border-t border-slate-700/50 animate-[fadeIn_0.5s_ease-out]">
      <div className="text-xs font-semibold text-slate-400 mb-2 flex items-center gap-1.5">
        <ExternalLink className="w-3 h-3" />
        Web Sources
      </div>
      <div className="flex flex-col gap-1.5">
        {references.map((url, i) => (
          <a
            key={i}
            href={url}
            target="_blank"
            rel="noopener noreferrer"
            className="text-xs text-blue-400 hover:text-blue-300 hover:underline truncate transition-colors flex items-center gap-1.5 group"
          >
            <span className="w-4 h-4 rounded bg-slate-700 flex items-center justify-center flex-shrink-0 text-[10px] font-medium text-slate-400 group-hover:bg-blue-500/20 group-hover:text-blue-400 transition-colors">
              {i + 1}
            </span>
            <span className="truncate">{url}</span>
          </a>
        ))}
      </div>
    </div>
  );
}

/* ─── Main App ─── */
export default function App() {
  const [token, setToken] = useState<string | null>(localStorage.getItem('rag_token'));
  const [roles, setRoles] = useState<string[]>(JSON.parse(localStorage.getItem('rag_roles') || '[]'));

  const [activeTab, setActiveTab] = useState<'chat' | 'knowledge' | 'audit' | 'users' | 'overview'>('chat');

  // Chat state
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [conversations, setConversations] = useState<string[]>([]);

  // Knowledge Base state
  const [documents, setDocuments] = useState<VectorDocument[]>([]);
  const [isDocumentsLoading, setIsDocumentsLoading] = useState(false);

  // Upload state
  const [isUploading, setIsUploading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // UI state
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const isAdmin = roles.includes('ROLE_ADMIN');

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (activeTab === 'chat') scrollToBottom();
  }, [messages, isLoading, activeTab]);

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

  useEffect(() => {
    if (inputRef.current) {
      inputRef.current.style.height = 'auto';
      inputRef.current.style.height = `${Math.min(inputRef.current.scrollHeight, 200)}px`;
    }
  }, [input]);

  const fetchConversations = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/documents/conversations', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (res.ok) setConversations(await res.json());
    } catch (e) { console.error(e); }
  };

  const loadConversation = async (id: string, updateState = false) => {
    if (updateState) {
      setConversationId(id);
      localStorage.setItem('rag_conversation_id', id);
      setActiveTab('chat');
      setIsSidebarOpen(false);
    }
    try {
      const res = await fetch(`http://localhost:8080/api/documents/chat/history?conversationId=${id}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (res.ok) {
        const history = await res.json();
        // History from backend doesn't have source/references, default to LOCAL
        setMessages(history.map((m: { role: string; content: string }) => ({
          ...m,
          source: 'LOCAL' as const,
          references: [],
        })));
      }
    } catch (e) { console.error(e); }
  };

  const loadDocuments = async () => {
    setIsDocumentsLoading(true);
    try {
      const res = await fetch('http://localhost:8080/api/documents', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (res.ok) setDocuments(await res.json());
    } catch (e) { console.error(e); }
    setIsDocumentsLoading(false);
  };

  useEffect(() => {
    if (activeTab === 'knowledge') loadDocuments();
  }, [activeTab]);

  const handleDeleteDocument = async (id: string) => {
    if (!confirm('Are you sure you want to delete this document?')) return;
    try {
      const res = await fetch(`http://localhost:8080/api/documents/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (res.ok) setDocuments(prev => prev.filter(d => d.id !== id));
    } catch (e) { console.error(e); }
  };

  const handleNewChat = () => {
    localStorage.removeItem('rag_conversation_id');
    setConversationId(null);
    setMessages([]);
    setActiveTab('chat');
    setIsSidebarOpen(false);
  };

  const handleClearHistory = () => {
    if (confirm('Are you sure you want to clear local history?')) handleNewChat();
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e as unknown as React.FormEvent);
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Reset input so the same file can be re-selected
    e.target.value = '';

    setIsUploading(true);
    setUploadStatus(`Nahrávám "${file.name}"...`);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const res = await fetch('http://localhost:8080/api/documents/upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData,
      });
      const data = res.ok ? await res.json() : { message: 'Upload failed' };

      if (res.ok) {
        setUploadStatus(`✓ "${file.name}" uploaded successfully!`);
        if (activeTab === 'knowledge') loadDocuments();
      } else {
        setUploadStatus(`✗ Error: ${data.message}`);
      }
    } catch (error) {
      console.error(error);
      setUploadStatus('✗ Error uploading file.');
    } finally {
      setIsUploading(false);
      setTimeout(() => setUploadStatus(null), 4000);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMessage = input.trim();
    setInput('');
    if (inputRef.current) inputRef.current.style.height = 'auto';

    setMessages((prev) => [...prev, { role: 'user', content: userMessage }]);
    setIsLoading(true);

    try {
      const params = new URLSearchParams({ query: userMessage });
      if (conversationId) params.append('conversationId', conversationId);

      const response = await fetch(`http://localhost:8080/api/documents/chat?${params.toString()}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
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

      setMessages((prev) => [...prev, {
        role: 'assistant',
        content: data.answer,
        source: data.source || 'LOCAL',
        references: data.references || [],
      }]);
    } catch (error) {
      console.error(error);
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: 'I am sorry, an error occurred while communicating with the server.' }
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('rag_token');
    localStorage.removeItem('rag_roles');
    setToken(null);
    setRoles([]);
  };

  // RBAC Guard
  useEffect(() => {
    if ((activeTab === 'audit' || activeTab === 'users' || activeTab === 'overview') && !isAdmin) {
      setActiveTab('chat');
    }
  }, [activeTab, isAdmin]);

  if (!token) {
    return (
      <Login
        onLoginSuccess={(newToken, newRoles) => {
          localStorage.setItem('rag_token', newToken);
          localStorage.setItem('rag_roles', JSON.stringify(newRoles));
          setToken(newToken);
          setRoles(newRoles);
        }}
      />
    );
  }

  return (
    <div className="flex h-screen bg-slate-950 text-slate-200 font-sans overflow-hidden">
      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept=".txt,.md,.pdf"
        className="hidden"
        onChange={handleFileUpload}
      />

      {/* Mobile Sidebar Overlay */}
      {isSidebarOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-20 md:hidden"
          onClick={() => setIsSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <div className={`
        fixed inset-y-0 left-0 z-30 w-72 bg-slate-900 border-r border-slate-800 transform transition-transform duration-300 ease-in-out flex flex-col
        md:relative md:translate-x-0
        ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        <div className="p-4 flex justify-between items-center md:hidden border-b border-slate-800">
          <span className="font-semibold text-slate-200 flex items-center gap-2">
            <Bot className="w-5 h-5 text-blue-500" />Menu
          </span>
          <button onClick={() => setIsSidebarOpen(false)} className="text-slate-400 hover:text-slate-200 p-1">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-3 space-y-2">
          <button
            onClick={handleNewChat}
            className="w-full flex items-center gap-2 px-3 py-3 rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition-colors shadow-sm font-medium text-sm"
          >
            <Plus className="w-4 h-4" />
            New Chat
          </button>

          <button
            onClick={() => { setActiveTab('knowledge'); setIsSidebarOpen(false); }}
            className={`w-full flex items-center gap-2 px-3 py-3 rounded-lg transition-colors font-medium text-sm ${activeTab === 'knowledge' ? 'bg-slate-800 text-blue-400' : 'text-slate-300 hover:bg-slate-800'}`}
          >
            <Database className="w-4 h-4" />
            Knowledge Base
          </button>
        </div>

        {isAdmin && (
          <div className="px-3 py-2 space-y-1 mt-2 border-t border-slate-800">
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2 px-2 mt-2">Admin Tools</div>
            <button
              onClick={() => { setActiveTab('overview'); setIsSidebarOpen(false); }}
              className={`w-full flex items-center gap-2 px-3 py-2.5 rounded-lg transition-colors font-medium text-sm ${activeTab === 'overview' ? 'bg-slate-800 text-blue-400' : 'text-slate-300 hover:bg-slate-800'}`}
            >
              <LayoutDashboard className="w-4 h-4" />
              Overview
            </button>
            <button
              onClick={() => { setActiveTab('users'); setIsSidebarOpen(false); }}
              className={`w-full flex items-center gap-2 px-3 py-2.5 rounded-lg transition-colors font-medium text-sm ${activeTab === 'users' ? 'bg-slate-800 text-purple-400' : 'text-slate-300 hover:bg-slate-800'}`}
            >
              <Users className="w-4 h-4" />
              Roles & Users
            </button>
            <button
              onClick={() => { setActiveTab('audit'); setIsSidebarOpen(false); }}
              className={`w-full flex items-center gap-2 px-3 py-2.5 rounded-lg transition-colors font-medium text-sm ${activeTab === 'audit' ? 'bg-slate-800 text-amber-400' : 'text-slate-300 hover:bg-slate-800'}`}
            >
              <ShieldAlert className="w-4 h-4" />
              Audit Dashboard
            </button>
          </div>
        )}

        <div className="flex-1 overflow-y-auto p-3">
          <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3 px-2">Conversation History</div>
          <div className="space-y-1">
            {conversations.length === 0 && (
              <div className="px-2 text-sm text-slate-500 italic">No history yet.</div>
            )}
            {conversations.map((id) => (
              <button
                key={id}
                onClick={() => loadConversation(id, true)}
                className={`w-full flex items-center gap-3 px-3 py-3 text-sm rounded-lg transition-colors text-left truncate ${conversationId === id && activeTab === 'chat' ? 'bg-slate-800 text-slate-200' : 'text-slate-400 hover:bg-slate-800/50'}`}
              >
                <MessageSquare className="w-4 h-4 flex-shrink-0" />
                <span className="truncate">Conversation {id.substring(0, 8)}</span>
              </button>
            ))}
          </div>
        </div>

        <div className="p-4 mt-auto border-t border-slate-800 space-y-2">
          <button
            onClick={handleClearHistory}
            className="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-400 hover:bg-red-400/10 rounded-lg transition-colors font-medium"
          >
            <Trash2 className="w-4 h-4" />
            Clear local context
          </button>
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-3 py-2 text-sm text-slate-400 hover:text-slate-200 hover:bg-slate-800 rounded-lg transition-colors font-medium border border-slate-700/50"
          >
            <X className="w-4 h-4" />
            Logout
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col relative w-full h-full bg-slate-950">
        {/* Header */}
        <header className="sticky top-0 bg-slate-900/80 backdrop-blur-md border-b border-slate-800 p-4 flex items-center justify-between z-10">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setIsSidebarOpen(true)}
              className="md:hidden p-1.5 text-slate-400 hover:bg-slate-800 rounded-md"
            >
              <Menu className="w-5 h-5" />
            </button>
            <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center shadow-sm">
              <Bot className="w-5 h-5 text-white" />
            </div>
            <h1 className="text-lg font-semibold text-slate-200 tracking-tight flex items-center gap-2">
              {activeTab === 'chat' && 'Second Brain RAG'}
              {activeTab === 'knowledge' && 'Knowledge Database'}
              {activeTab === 'users' && 'Roles & Users'}
              {activeTab === 'audit' && 'Audit Dashboard'}
              {activeTab === 'overview' && 'System Overview'}

              {roles.length > 0 && (
                <span className="text-[10px] font-medium uppercase tracking-wider text-blue-400 bg-blue-500/10 border border-blue-500/20 px-2 py-0.5 rounded-full">
                  {roles.join(', ')}
                </span>
              )}
            </h1>
          </div>
        </header>

        {/* Upload status toast */}
        {uploadStatus && (
          <div className="absolute top-16 left-1/2 -translate-x-1/2 z-50 bg-slate-800 border border-slate-700 rounded-xl px-4 py-3 text-sm text-slate-200 shadow-lg flex items-center gap-2 animate-[fadeIn_0.3s_ease-out]">
            {isUploading && <Loader2 className="w-4 h-4 animate-spin text-blue-400" />}
            {uploadStatus}
          </div>
        )}

        {activeTab === 'chat' ? (
          <>
            {/* Chat Area */}
            <main className="flex-1 overflow-y-auto scroll-smooth">
              {messages.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-full text-center px-4">
                  <div className="w-16 h-16 rounded-2xl bg-slate-800 flex items-center justify-center mb-6 shadow-sm border border-slate-700">
                    <Bot className="w-8 h-8 text-blue-500" />
                  </div>
                  <h2 className="text-2xl font-semibold text-slate-200 mb-2">How can I help you?</h2>
                  <p className="text-slate-400 max-w-md">I am your personal assistant connected to your knowledge database. Ask me anything from your documents.</p>
                </div>
              ) : (
                <div className="max-w-3xl mx-auto w-full py-6 sm:py-10 px-4 sm:px-6 flex flex-col gap-6">
                  {messages.map((msg, index) => (
                    <div
                      key={index}
                      className={`flex gap-4 sm:gap-6 ${msg.role === 'user' ? 'flex-row-reverse' : ''}`}
                    >
                      <div
                        className={`flex-shrink-0 w-8 h-8 mt-1 rounded-full flex items-center justify-center shadow-sm ${msg.role === 'user' ? 'bg-slate-700' : 'bg-blue-600'
                          }`}
                      >
                        {msg.role === 'user' ? (
                          <User className="w-5 h-5 text-slate-300" />
                        ) : (
                          <Bot className="w-5 h-5 text-white" />
                        )}
                      </div>

                      <div className={`max-w-[85%] sm:max-w-[75%] ${msg.role === 'user' ? '' : 'flex flex-col'}`}>
                        {/* Source Badge — only for assistant messages */}
                        {msg.role === 'assistant' && msg.source && (
                          <SourceBadge source={msg.source} />
                        )}

                        <div
                          className={`group relative px-5 py-3.5 text-[15px] leading-relaxed shadow-sm ${msg.role === 'user'
                            ? 'bg-blue-600 text-white rounded-3xl rounded-tr-sm'
                            : 'bg-slate-800 text-slate-200 rounded-3xl rounded-tl-sm border border-slate-700/50'
                            }`}
                        >
                          {msg.role === 'user' ? (
                            <div className="whitespace-pre-wrap">{msg.content}</div>
                          ) : (
                            <div className="prose prose-sm sm:prose-base prose-invert max-w-none prose-p:leading-relaxed prose-pre:bg-slate-900 prose-pre:border prose-pre:border-slate-700 prose-code:text-blue-400 prose-code:bg-slate-900 prose-code:px-1.5 prose-code:py-0.5 prose-code:rounded-md prose-code:before:content-none prose-code:after:content-none prose-a:text-blue-400 hover:prose-a:text-blue-300">
                              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                                {msg.content}
                              </ReactMarkdown>
                            </div>
                          )}

                          {/* References List — inside the bubble, below text */}
                          {msg.role === 'assistant' && msg.references && msg.references.length > 0 && (
                            <ReferencesList references={msg.references} />
                          )}

                          {/* Hallucination Warning — inside the bubble for WEB source */}
                          {msg.role === 'assistant' && msg.source === 'WEB' && (
                            <HallucinationWarning />
                          )}
                        </div>
                      </div>
                    </div>
                  ))}

                  {isLoading && (
                    <div className="flex gap-4 sm:gap-6">
                      <div className="flex-shrink-0 w-8 h-8 mt-1 rounded-full bg-blue-600 flex items-center justify-center shadow-sm">
                        <Bot className="w-5 h-5 text-white" />
                      </div>
                      <div className="bg-slate-800 text-slate-200 rounded-3xl rounded-tl-sm border border-slate-700/50 shadow-sm px-5 py-4 flex items-center gap-1.5 h-[52px]">
                        <div className="w-2 h-2 bg-blue-400 rounded-full animate-[pulse_1.5s_ease-in-out_infinite]" />
                        <div className="w-2 h-2 bg-blue-400 rounded-full animate-[pulse_1.5s_ease-in-out_0.2s_infinite]" />
                        <div className="w-2 h-2 bg-blue-400 rounded-full animate-[pulse_1.5s_ease-in-out_0.4s_infinite]" />
                      </div>
                    </div>
                  )}
                  <div ref={messagesEndRef} className="h-4" />
                </div>
              )}
            </main>

            {/* Input Area */}
            <footer className="p-4 bg-gradient-to-t from-slate-950 via-slate-950 to-transparent sticky bottom-0">
              <div className="max-w-3xl mx-auto relative">
                <form
                  onSubmit={handleSubmit}
                  className="relative flex items-end bg-slate-800/80 border border-slate-700 rounded-[24px] shadow-sm hover:shadow-md transition-shadow focus-within:shadow-md focus-within:border-slate-600 focus-within:bg-slate-800 overflow-hidden"
                >
                  {/* Paperclip upload button */}
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={isUploading}
                    className="absolute left-3 bottom-3 p-1.5 text-slate-400 hover:text-slate-200 hover:bg-slate-700 rounded-full transition-colors disabled:opacity-40"
                    title="Nahrát soubor (.txt, .md)"
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
          </>
        ) : activeTab === 'knowledge' ? (
          /* Knowledge Base Tab */
          <main className="flex-1 overflow-y-auto p-6 scroll-smooth">
            <div className="max-w-4xl mx-auto space-y-6">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-semibold text-slate-200 flex items-center gap-2">
                  <Database className="w-5 h-5 text-blue-500" /> Documents in Database
                </h2>
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => fileInputRef.current?.click()}
                    disabled={isUploading}
                    className="flex items-center gap-2 px-3 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium disabled:opacity-50"
                  >
                    {isUploading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Paperclip className="w-4 h-4" />}
                    Upload File
                  </button>
                  <div className="text-sm text-slate-400">{documents.length} chunks</div>
                </div>
              </div>

              {isDocumentsLoading ? (
                <div className="flex items-center justify-center py-12 text-slate-400">
                  <Loader2 className="w-5 h-5 animate-spin mr-2" /> Loading...
                </div>
              ) : documents.length === 0 ? (
                <div className="text-center py-12 text-slate-500 border border-slate-800 border-dashed rounded-xl bg-slate-900/50">
                  The database is currently empty. Upload a file using the button above.
                </div>
              ) : (
                <div className="grid gap-4">
                  {documents.map(doc => (
                    <div key={doc.id} className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex gap-4 hover:border-slate-700 transition-colors">
                      <div className="mt-1 flex-shrink-0">
                        <FileText className="w-5 h-5 text-slate-500" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-2">
                          <div className="font-mono text-xs text-slate-500 truncate">ID: {doc.id.substring(0, 8)}…</div>
                          {(doc.metadata?.fileName || doc.metadata?.source) && (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium bg-blue-500/15 border border-blue-500/30 text-blue-400">
                              📄 {doc.metadata.fileName || doc.metadata.source}
                            </span>
                          )}
                          {doc.metadata?.documentType && (
                            <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-slate-700 text-slate-400 uppercase">
                              {doc.metadata.documentType}
                            </span>
                          )}
                        </div>
                        <div className="text-sm text-slate-300 leading-relaxed line-clamp-3">
                          {doc.content}
                        </div>
                      </div>
                      <div className="flex-shrink-0 w-8">
                        <button
                          onClick={() => handleDeleteDocument(doc.id)}
                          className="p-1.5 text-slate-500 hover:text-red-400 hover:bg-slate-800 rounded-md transition-colors"
                          title="Delete Document"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </main>
        ) : activeTab === 'users' && isAdmin ? (
          <main className="flex-1 overflow-y-auto p-6 scroll-smooth bg-slate-950">
            <div className="max-w-4xl mx-auto">
              <UserList token={token} currentUsername={messages[0] ? token : 'TY'} />
            </div>
          </main>
        ) : activeTab === 'audit' && isAdmin ? (
          <main className="flex-1 overflow-y-auto p-6 scroll-smooth bg-slate-950">
            <div className="max-w-6xl mx-auto">
              <AuditLogs token={token} />
            </div>
          </main>
        ) : activeTab === 'overview' && isAdmin ? (
          <main className="flex-1 overflow-y-auto p-6 scroll-smooth bg-slate-950">
            <div className="max-w-6xl mx-auto">
              <AdminOverview token={token} />
            </div>
          </main>
        ) : null}
      </div>
    </div>
  );
}
