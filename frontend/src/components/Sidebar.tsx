import { User, Plus, Database, ShieldAlert, LayoutDashboard, Users, MessageSquare, Trash2, X, LogOut } from 'lucide-react';

interface SidebarProps {
  isOpen: boolean;
  setIsOpen: (isOpen: boolean) => void;
  isAdmin: boolean;
  activeTab: 'chat' | 'knowledge' | 'users' | 'audit' | 'overview';
  setActiveTab: (tab: 'chat' | 'knowledge' | 'users' | 'audit' | 'overview') => void;
  conversations: string[];
  conversationId: string | null;
  loadConversation: (id: string, updateState: boolean) => void;
  handleNewChat: () => void;
  handleClearHistory: () => void;
  currentUsername: string;
  handleLogout: () => void;
}

export function Sidebar({
  isOpen, setIsOpen, isAdmin, activeTab, setActiveTab,
  conversations, conversationId, loadConversation, handleNewChat, handleClearHistory, currentUsername, handleLogout
}: SidebarProps) {
  return (
    <aside className={`fixed md:static inset-y-0 left-0 w-72 h-full flex-shrink-0 bg-slate-900 border-r border-slate-800 flex flex-col transition-transform duration-300 ease-in-out z-50 shadow-2xl md:shadow-none ${isOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'}`}>
        {/* User Profile Area */}
        <div className="p-5 border-b border-slate-800 bg-slate-900 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-blue-500/20 flex items-center justify-center border border-blue-500/30">
              <User className="w-5 h-5 text-blue-400" />
            </div>
            <div className="flex flex-col">
              <span className="text-sm font-semibold text-slate-200">
                {currentUsername}
              </span>
              <span className="text-[10px] text-slate-500 font-medium uppercase tracking-wider flex items-center gap-1">
                <div className={`w-1.5 h-1.5 rounded-full ${isAdmin ? 'bg-amber-400' : 'bg-emerald-400'}`}></div>
                {isAdmin ? 'Administrator' : 'User'}
              </span>
            </div>
          </div>
          <div className="flex items-center gap-1">
            <button 
              onClick={handleLogout}
              className="p-2 text-slate-400 hover:text-red-400 hover:bg-slate-800 rounded-lg transition-colors"
              title="Sign Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
            <button 
              onClick={() => setIsOpen(false)}
              className="md:hidden p-2 text-slate-400 hover:text-slate-200 hover:bg-slate-800 rounded-lg transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Global Actions */}
        <div className="p-4 space-y-3">
          <button
            onClick={() => { handleNewChat(); setIsOpen(false); setActiveTab('chat'); }}
            className="w-full flex items-center justify-center gap-2 px-4 py-4 rounded-xl bg-blue-600 text-white hover:bg-blue-700 transition-all shadow-lg hover:shadow-blue-500/20 font-bold text-base active:scale-95 group"
          >
            <Plus className="w-5 h-5 group-hover:rotate-90 transition-transform" />
            New Chat
          </button>

          <div className="grid grid-cols-1 gap-2">
            <button
              onClick={() => { setActiveTab('knowledge'); setIsOpen(false); }}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors font-medium text-sm ${activeTab === 'knowledge' ? 'bg-slate-800 text-blue-400 border border-blue-500/20' : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'}`}
            >
              <Database className="w-4 h-4" />
              Knowledge Base
            </button>
          </div>
        </div>

        {/* Admin Section */}
        {isAdmin && (
          <div className="px-3 pb-2 pt-1">
            <div className="px-3 mb-2 text-xs font-bold text-slate-600 uppercase tracking-widest">
              Administration
            </div>
            <div className="space-y-1">
              <button
                onClick={() => { setActiveTab('overview'); setIsOpen(false); }}
                className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${activeTab === 'overview' ? 'bg-slate-800 text-indigo-400 border border-indigo-500/20' : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'}`}
              >
                <LayoutDashboard className="w-4 h-4" />
                Overview
              </button>
              <button
                onClick={() => { setActiveTab('users'); setIsOpen(false); }}
                className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${activeTab === 'users' ? 'bg-slate-800 text-indigo-400 border border-indigo-500/20' : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'}`}
              >
                <Users className="w-4 h-4" />
                User Management
              </button>
              <button
                onClick={() => { setActiveTab('audit'); setIsOpen(false); }}
                className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${activeTab === 'audit' ? 'bg-slate-800 text-indigo-400 border border-indigo-500/20' : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'}`}
              >
                <ShieldAlert className="w-4 h-4" />
                Audit Logs
              </button>
            </div>
          </div>
        )}

        {/* Conversation History */}
        <div className="px-3 pt-4 border-t border-slate-800/50 mt-2 mb-2">
          <div className="px-3 mb-3 flex items-center justify-between text-xs font-bold text-slate-600 uppercase tracking-widest">
            <span>Conversations</span>
            <button 
              onClick={() => handleClearHistory()}
              className="hover:text-red-400 transition-colors"
              title="Clear Local History"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-3 pb-4 space-y-1 scroll-smooth">
          {conversations.length === 0 ? (
            <div className="text-center py-6 px-4">
              <div className="w-12 h-12 rounded-full bg-slate-800/50 flex items-center justify-center mx-auto mb-3">
                <MessageSquare className="w-5 h-5 text-slate-600" />
              </div>
              <p className="text-sm text-slate-500">No previous conversations yet</p>
            </div>
          ) : (
            conversations.map(id => (
              <button
                key={id}
                onClick={() => {
                  loadConversation(id, true);
                  setActiveTab('chat');
                  setIsOpen(false);
                }}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all group ${
                  conversationId === id && activeTab === 'chat'
                    ? 'bg-blue-600/10 text-blue-400 font-medium' 
                    : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
                }`}
              >
                <MessageSquare className={`w-4 h-4 ${conversationId === id && activeTab === 'chat' ? 'text-blue-500' : 'text-slate-500 group-hover:text-slate-400'}`} />
                <span className="truncate flex-1 text-left">Chat {id.substring(0, 8)}</span>
              </button>
            ))
          )}
        </div>
      </aside>
  );
}
