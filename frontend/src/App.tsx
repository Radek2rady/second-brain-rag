import { useState } from 'react';
import Login from './Login';
import UserList from './UserList';
import AuditLogs from './AuditLogs';
import AdminOverview from './AdminOverview';

// Hooks
import { useAuth } from './hooks/useAuth';
import { useChat } from './hooks/useChat';
import { useKnowledgeBase } from './hooks/useKnowledgeBase';

// Components
import { Sidebar } from './components/Sidebar';
import { TopBar } from './components/TopBar';
import { ChatWindow } from './components/ChatWindow';
import { KnowledgeBase } from './components/KnowledgeBase';

type Tab = 'chat' | 'knowledge' | 'users' | 'audit' | 'overview';

export default function App() {
  const { token, isAdmin, currentUsername, login, logout } = useAuth();
  
  const [activeTab, setActiveTab] = useState<Tab>('chat');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  // Chat Hook
  const {
    messages,
    isLoading,
    conversationId,
    conversations,
    sendMessage,
    loadConversation,
    handleNewChat,
    handleClearHistory,
  } = useChat(token);

  // Knowledge Base Hook
  const {
    documents,
    isDocumentsLoading,
    loadDocuments,
    handleDeleteDocument
  } = useKnowledgeBase(token, activeTab === 'knowledge');

  if (!token) {
    return <Login onLoginSuccess={login} />;
  }

  return (
    <div className="relative flex h-[100dvh] w-full max-w-[100vw] bg-slate-950 text-slate-200 overflow-hidden font-sans selection:bg-blue-500/30">
      {/* Mobile Sidebar Overlay */}
      {isSidebarOpen && (
        <div 
          className="fixed inset-0 bg-slate-900/80 backdrop-blur-sm z-40 md:hidden"
          onClick={() => setIsSidebarOpen(false)}
        />
      )}

      <Sidebar
        isOpen={isSidebarOpen}
        setIsOpen={setIsSidebarOpen}
        isAdmin={isAdmin}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        conversations={conversations}
        conversationId={conversationId}
        loadConversation={loadConversation}
        handleNewChat={handleNewChat}
        handleClearHistory={handleClearHistory}
        currentUsername={currentUsername || 'User'}
        handleLogout={logout}
      />

      <div className="flex-1 flex flex-col min-w-0 h-full overflow-hidden relative">
        <TopBar setIsOpen={setIsSidebarOpen} />
        
        {activeTab === 'chat' ? (
          <ChatWindow
            messages={messages}
            isLoading={isLoading}
            sendMessage={sendMessage}
            token={token}
            isAdmin={isAdmin}
          />
        ) : activeTab === 'knowledge' ? (
          <KnowledgeBase
            documents={documents}
            isDocumentsLoading={isDocumentsLoading}
            handleDeleteDocument={handleDeleteDocument}
            isAdmin={isAdmin}
            token={token}
            onUploadSuccess={loadDocuments}
          />
        ) : activeTab === 'users' && isAdmin ? (
          <main className="flex-1 overflow-y-auto p-6 scroll-smooth bg-slate-950">
            <div className="max-w-4xl mx-auto">
              {/* Note: currentUsername was passed here previously */}
              <UserList token={token} currentUsername={currentUsername || 'User'} />
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
