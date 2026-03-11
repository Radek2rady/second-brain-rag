import { Menu, Blend, Bot } from 'lucide-react';

interface TopBarProps {
  setIsOpen: (isOpen: boolean) => void;
}

export function TopBar({ setIsOpen }: TopBarProps) {
  return (
    <header className="h-14 bg-slate-900 border-b border-slate-800 flex items-center justify-between px-4 md:hidden sticky top-0 z-30 shadow-sm">
      <div className="flex items-center gap-2">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500/20 to-indigo-500/20 flex items-center justify-center border border-blue-500/50 shadow-sm">
          <Bot className="w-5 h-5 text-blue-400" />
        </div>
        <span className="font-bold text-slate-200 tracking-tight text-lg flex items-center gap-1.5">
          Brain<Blend className="w-4 h-4 text-blue-500" />RAG
        </span>
      </div>
      <button 
        onClick={() => setIsOpen(true)}
        className="p-2 -mr-2 text-slate-400 hover:text-slate-200 hover:bg-slate-800 rounded-lg transition-colors"
      >
        <Menu className="w-5 h-5" />
      </button>
    </header>
  );
}
