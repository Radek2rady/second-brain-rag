import { useState, useEffect } from 'react';
import type { VectorDocument } from '../types';
import apiClient from '../api/client';

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
      const res = await apiClient.get('/api/documents');
      setDocuments(res.data);
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
      await apiClient.delete(`/api/documents/${id}`);
      setDocuments(prev => prev.filter(d => d.id !== id));
    } catch (e) { console.error(e); }
  };

  const uploadFile = async (file: File, accessLevel: string, onSuccess?: () => void) => {
    if (!token) return;
    setIsUploading(true);
    setUploadStatus(`Uploading "${file.name}"...`);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('accessLevel', accessLevel);

    try {
      await apiClient.post('/api/documents/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      setUploadStatus(`✓ "${file.name}" uploaded successfully!`);
      if (onSuccess) onSuccess();
    } catch (error: any) {
      console.error(error);
      const message = error.response?.data?.message || 'Upload failed';
      setUploadStatus(`✗ Error: ${message}`);
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
