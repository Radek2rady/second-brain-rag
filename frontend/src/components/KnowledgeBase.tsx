import { Database, Paperclip, Loader2, FileText, Trash2 } from 'lucide-react';
import type { VectorDocument } from '../types';

interface KnowledgeBaseProps {
  documents: VectorDocument[];
  isDocumentsLoading: boolean;
  isUploading: boolean;
  fileInputRef: React.RefObject<HTMLInputElement>;
  handleDeleteDocument: (id: string) => void;
}

export function KnowledgeBase({
  documents, isDocumentsLoading, isUploading, fileInputRef, handleDeleteDocument
}: KnowledgeBaseProps) {
  return (
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
  );
}
