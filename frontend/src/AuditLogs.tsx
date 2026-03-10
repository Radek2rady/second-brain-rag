import React, { useState, useEffect } from 'react';
import { Loader2, ShieldAlert, ChevronLeft, ChevronRight } from 'lucide-react';

interface AuditEvent {
    id: string;
    timestamp: string;
    username: string;
    tenantId: string;
    action: string;
    details: string;
    status: string;
}

interface PageData {
    content: AuditEvent[];
    totalPages: number;
    totalElements: number;
    number: number;
    first: boolean;
    last: boolean;
}

interface AuditLogsProps {
    token: string;
}

export default function AuditLogs({ token }: AuditLogsProps) {
    const [data, setData] = useState<PageData | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [page, setPage] = useState(0);

    const fetchLogs = async (pageNumber: number) => {
        setIsLoading(true);
        try {
            const res = await fetch(`http://localhost:8080/api/audit?page=${pageNumber}&size=10`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (res.ok) {
                setData(await res.json());
            } else {
                console.error("Failed to fetch audit logs");
            }
        } catch (e) {
            console.error(e);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchLogs(page);
    }, [page, token]);

    return (
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden mt-6 shadow-sm">
            <div className="p-4 border-b border-slate-800 flex justify-between items-center bg-slate-900/50">
                <h2 className="font-semibold text-slate-200">Audit Dashboard</h2>
                <div className="text-sm text-slate-400">
                    Celkem záznamů: {data?.totalElements || 0}
                </div>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-sm text-left">
                    <thead className="text-xs text-slate-400 uppercase bg-slate-800/50 border-b border-slate-700">
                        <tr>
                            <th className="px-4 py-3">Čas</th>
                            <th className="px-4 py-3">Uživatel</th>
                            <th className="px-4 py-3">Akce</th>
                            <th className="px-4 py-3">Detaily</th>
                            <th className="px-4 py-3 text-right">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading && !data ? (
                            <tr>
                                <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
                                    <Loader2 className="w-5 h-5 animate-spin mx-auto mb-2" />
                                    Načítám logy...
                                </td>
                            </tr>
                        ) : data?.content.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
                                    Zatím žádné auditní záznamy.
                                </td>
                            </tr>
                        ) : (
                            data?.content.map((log) => (
                                <tr key={log.id} className={`border-b border-slate-800/50 hover:bg-slate-800/30 transition-colors ${log.status === 'DENIED' ? 'bg-red-500/5 hover:bg-red-500/10' : ''}`}>
                                    <td className="px-4 py-3 whitespace-nowrap text-slate-400">
                                        {new Date(log.timestamp).toLocaleString('cs-CZ')}
                                    </td>
                                    <td className="px-4 py-3 font-medium text-slate-300">
                                        {log.username}
                                    </td>
                                    <td className="px-4 py-3">
                                        <span className="bg-slate-800 text-slate-300 px-2 py-1 rounded text-xs border border-slate-700">
                                            {log.action}
                                        </span>
                                    </td>
                                    <td className="px-4 py-3 text-slate-400 max-w-xs truncate" title={log.details}>
                                        {log.details}
                                    </td>
                                    <td className="px-4 py-3 text-right whitespace-nowrap">
                                        {log.status === 'SUCCESS' && (
                                            <span className="text-emerald-400 bg-emerald-400/10 px-2 py-1 rounded border border-emerald-400/20 text-xs font-medium">SUCCESS</span>
                                        )}
                                        {log.status === 'DENIED' && (
                                            <span className="text-red-400 bg-red-400/10 px-2 py-1 rounded border border-red-400/20 text-xs font-medium flex items-center justify-end gap-1 w-fit ml-auto">
                                                <ShieldAlert className="w-3 h-3" /> DENIED
                                            </span>
                                        )}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Pagination Controls */}
            {data && data.totalPages > 1 && (
                <div className="p-4 border-t border-slate-800 flex items-center justify-between bg-slate-900/50">
                    <span className="text-sm text-slate-400">
                        Strana <span className="font-medium text-slate-300">{data.number + 1}</span> z <span className="font-medium text-slate-300">{data.totalPages}</span>
                    </span>
                    <div className="flex gap-2">
                        <button
                            onClick={() => setPage(p => Math.max(0, p - 1))}
                            disabled={data.first || isLoading}
                            className="p-1.5 rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed border border-slate-700"
                        >
                            <ChevronLeft className="w-4 h-4" />
                        </button>
                        <button
                            onClick={() => setPage(p => Math.min(data.totalPages - 1, p + 1))}
                            disabled={data.last || isLoading}
                            className="p-1.5 rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed border border-slate-700"
                        >
                            <ChevronRight className="w-4 h-4" />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
