import { useEffect, useRef, useState } from 'react';
import { Loader2, Paperclip } from 'lucide-react';
import apiClient from '../api/client';

interface FileUploadProps {
  token: string | null;
  isAdmin: boolean;
  onUploadSuccess?: () => void;
  minimal?: boolean;
}

export function FileUpload({ token, isAdmin, onUploadSuccess, minimal = false }: FileUploadProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [accessLevel, setAccessLevel] = useState('PRIVATE');
  const [isUploading, setIsUploading] = useState(false);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    if (!isAdmin && accessLevel === 'GLOBAL') {
      setAccessLevel('PRIVATE');
    }
  }, [isAdmin, accessLevel]);

  const handleUpload = async (file: File) => {
    if (!token) return;
    setIsUploading(true);
    setStatus(`Uploading "${file.name}"...`);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('accessLevel', accessLevel);

    try {
      await apiClient.post('/api/documents/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      setStatus(`"${file.name}" uploaded successfully.`);
      if (onUploadSuccess) onUploadSuccess();
    } catch (error: any) {
      console.error(error);
      const message = error.response?.data?.message || 'Upload failed';
      setStatus(`Error: ${message}`);
    } finally {
      setIsUploading(false);
      setTimeout(() => setStatus(null), 3000);
    }
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    await handleUpload(file);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div className={minimal ? "flex items-center gap-2" : "flex items-center gap-3"}>
      <input
        type="file"
        ref={fileInputRef}
        className="hidden"
        accept=".txt,.md"
        onChange={handleFileChange}
      />
      <select
        value={accessLevel}
        onChange={(e) => setAccessLevel(e.target.value)}
        disabled={isUploading}
        className={
          minimal
            ? "w-[120px] bg-slate-900 border border-slate-700 text-slate-200 text-xs rounded-lg focus:ring-blue-500 focus:border-blue-500 block px-2 py-2 outline-none disabled:opacity-50"
            : "bg-slate-900 border border-slate-700 text-slate-200 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block p-2 outline-none disabled:opacity-50"
        }
      >
        <option value="PRIVATE">Jen pro mě</option>
        <option value="COMPANY">Pro firmu</option>
        {isAdmin && <option value="GLOBAL">Veřejné</option>}
      </select>
      <button
        onClick={() => fileInputRef.current?.click()}
        disabled={isUploading}
        title="Upload File (.txt, .md)"
        className={
          minimal
            ? "flex items-center justify-center p-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium disabled:opacity-50"
            : "flex items-center gap-2 px-3 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium disabled:opacity-50"
        }
      >
        {isUploading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Paperclip className="w-4 h-4" />}
        {!minimal && "Upload File"}
      </button>
      {!minimal && status && (
        <div className="text-xs text-slate-400 max-w-[220px] truncate" title={status}>
          {status}
        </div>
      )}
    </div>
  );
}
