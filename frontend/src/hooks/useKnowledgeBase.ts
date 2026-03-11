import { useState, useEffect } from 'react';
import type { VectorDocument } from '../types';

export function useKnowledgeBase(token: string | null, autoLoad: boolean = false) {
  const [documents, setDocuments] = useState<VectorDocument[]>([]);
  const [isDocumentsLoading, setIsDocumentsLoading] = useState(false);
  
  // Upload State
  const [isUploading, setIsUploading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState<string | null>(null);

  const loadDocuments = async () => {
    if (!token) return;
    setIsDocumentsLoading(true);
    try {
      const res = await fetch('http://localhost:8080/api/documents', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) setDocuments(await res.json());
    } catch (e) { console.error(e); }
    setIsDocumentsLoading(false);
  };

  useEffect(() => {
    if (autoLoad && token) {
      loadDocuments();
    }
  }, [autoLoad, token]);

  const handleDeleteDocument = async (id: string) => {
    if (!token || !confirm('Are you sure you want to delete this document?')) return;
    try {
      const res = await fetch(`http://localhost:8080/api/documents/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) setDocuments(prev => prev.filter(d => d.id !== id));
    } catch (e) { console.error(e); }
  };

  const uploadFile = async (file: File, onSuccess?: () => void) => {
    if (!token) return;
    setIsUploading(true);
    setUploadStatus(`Uploading "${file.name}"...`);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('http://localhost:8080/api/documents/upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData,
      });
      
      const data = response.ok ? await response.json() : { message: 'Upload failed' };

      if (response.ok) {
        setUploadStatus(`✓ "${file.name}" uploaded successfully!`);
        if (onSuccess) onSuccess();
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

  return {
    documents,
    isDocumentsLoading,
    isUploading,
    uploadStatus,
    loadDocuments,
    handleDeleteDocument,
    uploadFile
  };
}
