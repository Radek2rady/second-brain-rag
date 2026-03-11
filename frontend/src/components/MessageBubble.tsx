import { ExternalLink, FileText, Bot, User } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import type { ChatMessage } from '../types';

interface MessageBubbleProps {
  msg: ChatMessage;
  index: number;
}

export function MessageBubble({ msg }: MessageBubbleProps) {
  const isAssistant = msg.role === 'assistant';

  // Function to process markdown and make citations interactive
  const renderInteractiveMarkdown = (text: string) => {
    return (
      <ReactMarkdown 
        remarkPlugins={[remarkGfm]}
        components={{
          p: ({node, ...props}) => <p className="mb-3 last:mb-0 leading-relaxed text-slate-300" {...props} />,
          ul: ({node, ...props}) => <ul className="list-disc pl-6 mb-3 space-y-1.5 text-slate-300 marker:text-slate-500" {...props} />,
          ol: ({node, ...props}) => <ol className="list-decimal pl-6 mb-3 space-y-1.5 text-slate-300 marker:text-slate-500" {...props} />,
          li: ({node, ...props}) => <li className="pl-1" {...props} />,
          strong: ({node, ...props}) => <strong className="font-semibold text-slate-200" {...props} />,
          h1: ({node, ...props}) => <h1 className="text-xl font-bold mt-6 mb-4 text-slate-200" {...props} />,
          h2: ({node, ...props}) => <h2 className="text-lg font-bold mt-5 mb-3 text-slate-200" {...props} />,
          h3: ({node, ...props}) => <h3 className="text-md font-bold mt-4 mb-2 text-slate-200" {...props} />,
          blockquote: ({node, ...props}) => <blockquote className="border-l-4 border-slate-600 pl-4 py-1 mb-3 text-slate-400 bg-slate-800/30 rounded-r text-sm italic" {...props} />,
          code: ({node, inline, ...props}: any) => 
            inline 
              ? <code className="bg-slate-800 px-1.5 py-0.5 rounded text-[13px] font-mono text-blue-300" {...props} />
              : <code className="block bg-slate-900 overflow-x-auto p-4 rounded-lg border border-slate-700 font-mono text-[13px] text-slate-300 mb-3 whitespace-pre" {...props} />,
          a: ({node, href, children, ...props}) => {
             // Handle citations like [1]
             if (children && typeof children === 'string' && /^\[\d+\]$/.test(children)) {
               return (
                <a 
                  href={`#ref-${children.replace(/[\[\]]/g, '')}`} 
                  className="inline-flex items-center justify-center w-5 h-5 mx-0.5 text-[10px] font-bold bg-blue-500/20 text-blue-400 rounded hover:bg-blue-500/40 transition-colors no-underline cursor-pointer align-super"
                  title="Go to source"
                  {...props}
                >
                  {children.replace(/[\[\]]/g, '')}
                </a>
               );
             }
             return <a href={href} className="text-blue-400 hover:text-blue-300 underline underline-offset-2" target="_blank" rel="noopener noreferrer" {...props}>{children}</a>;
          }
        }}
      >
        {/* Pre-process text to wrap [1] citations in markdown links so our custom 'a' renderer catches them */}
        {text.replace(/(\[\d+\])/g, '[$1]($1)')}
      </ReactMarkdown>
    );
  };

  return (
    <div className={`flex gap-4 p-4 rounded-xl transition-all ${isAssistant ? 'bg-slate-800/40 border border-slate-700/50 flex-col sm:flex-row' : ''}`}>
      <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 mt-1 shadow-sm ${isAssistant ? 'bg-gradient-to-br from-indigo-500 to-blue-600' : 'bg-slate-700'}`}>
        {isAssistant ? <Bot className="w-5 h-5 text-white" /> : <User className="w-4 h-4 text-slate-300" />}
      </div>
      
      <div className="flex-1 min-w-0">
        <div className="font-semibold text-sm mb-1">
          {isAssistant ? (
            <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-indigo-400">Second Brain</span>
          ) : (
            <span className="text-slate-400">You</span>
          )}
        </div>
        
        <div className="text-[15px] prose prose-invert max-w-none">
          {isAssistant ? renderInteractiveMarkdown(msg.content) : msg.content}
        </div>
        
        {isAssistant && msg.references && msg.references.length > 0 && (
          <ReferencesList references={msg.references} />
        )}
      </div>
    </div>
  );
}

function ReferencesList({ references }: { references: string[] }) {
  if (references.length === 0) return null;

  const isUrl = (str: string) => str.startsWith('http://') || str.startsWith('https://');

  return (
    <div className="mt-3 pt-3 border-t border-slate-700/50 animate-[fadeIn_0.5s_ease-out]">
      <div className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2 flex items-center gap-1.5">
        <ExternalLink className="w-3 h-3" />
        Sources used
      </div>
      <div className="flex flex-wrap gap-2">
        {references.map((ref, i) => {
          const citationId = `ref-${i + 1}`;
          return isUrl(ref) ? (
            <a
              id={citationId}
              key={i}
              href={ref}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 px-2 py-1 rounded-md bg-blue-500/10 border border-blue-500/20 text-[11px] text-blue-400 hover:bg-blue-500/20 transition-colors max-w-[200px] target:ring-2 target:ring-blue-400"
            >
              <span className="w-3.5 h-3.5 rounded bg-blue-500/20 flex items-center justify-center text-[9px] font-bold">
                {i + 1}
              </span>
              <span className="truncate">{ref}</span>
            </a>
          ) : (
            <div
              id={citationId}
              key={i}
              className="inline-flex items-center gap-1.5 px-2 py-1 rounded-md bg-slate-700/50 border border-slate-700 text-[11px] text-slate-300 max-w-[200px] target:ring-2 target:ring-blue-400"
            >
              <span className="w-3.5 h-3.5 rounded bg-slate-600 flex items-center justify-center text-[9px] font-bold text-slate-300">
                {i + 1}
              </span>
              <FileText className="w-3 h-3 text-slate-400" />
              <span className="truncate">{ref}</span>
            </div>
          )
        })}
      </div>
    </div>
  );
}
