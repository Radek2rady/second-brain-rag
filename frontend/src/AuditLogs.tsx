import { useState, useEffect } from 'react';
import { Loader2, ShieldAlert, ChevronLeft, ChevronRight } from 'lucide-react';
import apiClient from './api/client';

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
    const [usernameFilter, setUsernameFilter] = useState('');
    const [actionFilter, setActionFilter] = useState('');

    const fetchLogs = async (pageNumber: number) => {
        setIsLoading(true);
        try {
            const params = {
                page: pageNumber.toString(),
                size: '10',
                username: usernameFilter,
                action: actionFilter
            };
            const res = await apiClient.get('/api/audit', { params });
            setData(res.data);
        } catch (e) {
            console.error(e);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        // Reset to page 0 when filters change, but we don't fetch on every keystroke
        // unless requested. Just relying on the generic useEffect.
        setPage(0);
    }, [usernameFilter, actionFilter]);

    useEffect(() => {
        fetchLogs(page);
    }, [page, token, usernameFilter, actionFilter]);

    return (
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden mt-6 shadow-sm">
            <div className="p-4 border-b border-slate-800 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-slate-900/50">
                <h2 className="font-semibold text-slate-200">Audit Dashboard</h2>
                <div className="flex flex-1 max-w-md gap-3 w-full">
                    <input
                        type="text"
                        placeholder="Filter by User..."
                        value={usernameFilter}
                        onChange={(e) => setUsernameFilter(e.target.value)}
                        className="flex-1 px-3 py-1.5 text-sm bg-slate-800 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-blue-500 placeholder-slate-500 transition-shadow transition-colors"
                    />
                    <input
                        type="text"
                        placeholder="Filter by Action..."
                        value={actionFilter}
                        onChange={(e) => setActionFilter(e.target.value)}
                        className="flex-1 px-3 py-1.5 text-sm bg-slate-800 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-blue-500 placeholder-slate-500 transition-shadow transition-colors"
                    />
                </div>
                <div className="text-sm text-slate-400 whitespace-nowrap">
                    Total records: {data?.totalElements || 0}
                </div>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-sm text-left">
                    <thead className="text-xs text-slate-400 uppercase bg-slate-800/50 border-b border-slate-700">
                        <tr>
                            <th className="px-4 py-3">Time</th>
                            <th className="px-4 py-3">User</th>
                            <th className="px-4 py-3">Action</th>
                            <th className="px-4 py-3">Details</th>
                            <th className="px-4 py-3 text-right">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading && !data ? (
                            <tr>
                                <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
                                    <Loader2 className="w-5 h-5 animate-spin mx-auto mb-2" />
                                    Loading logs...
                                </td>
                            </tr>
                        ) : data?.content.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
                                    No audit records found.
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
                        Page <span className="font-medium text-slate-300">{data.number + 1}</span> of <span className="font-medium text-slate-300">{data.totalPages}</span>
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
