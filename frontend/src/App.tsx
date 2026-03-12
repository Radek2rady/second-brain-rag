import { useState, useRef } from 'react';
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
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploadAccessLevel, setUploadAccessLevel] = useState('PRIVATE');

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
    isUploading,
    uploadStatus,
    loadDocuments,
    handleDeleteDocument,
    uploadFile
  } = useKnowledgeBase(token, activeTab === 'knowledge');

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      await uploadFile(file, uploadAccessLevel, () => {
        if (activeTab === 'knowledge') {
          loadDocuments();
        }
      });
      if (fileInputRef.current) {
         fileInputRef.current.value = '';
      }
    }
  };

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
        
        <input 
          type="file" 
          ref={fileInputRef} 
          className="hidden" 
          accept=".txt,.md" 
          onChange={handleFileChange}
        />

        {activeTab === 'chat' ? (
          <ChatWindow
            messages={messages}
            isLoading={isLoading}
            sendMessage={sendMessage}
            isUploading={isUploading}
            uploadStatus={uploadStatus}
            fileInputRef={fileInputRef as React.RefObject<HTMLInputElement>}
          />
        ) : activeTab === 'knowledge' ? (
          <KnowledgeBase
            documents={documents}
            isDocumentsLoading={isDocumentsLoading}
            isUploading={isUploading}
            fileInputRef={fileInputRef as React.RefObject<HTMLInputElement>}
            handleDeleteDocument={handleDeleteDocument}
            isAdmin={isAdmin}
            uploadAccessLevel={uploadAccessLevel}
            setUploadAccessLevel={setUploadAccessLevel}
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
