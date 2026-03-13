import { useState, useEffect } from 'react';
import { Loader2, FileText, Activity, Users, TrendingUp } from 'lucide-react';
import apiClient from './api/client';

interface UserActivity {
    username: String;
    count: number;
}

interface AdminStats {
    totalDocuments: number;
    auditEvents24h: number;
    topUsers: UserActivity[];
}

interface AdminOverviewProps {
    token: string;
}

export default function AdminOverview({ token }: AdminOverviewProps) {
    const [stats, setStats] = useState<AdminStats | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const fetchStats = async () => {
        setIsLoading(true);
        try {
            const res = await apiClient.get('/api/admin/stats');
            setStats(res.data);
        } catch (e) {
            console.error(e);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchStats();
    }, [token]);

    if (isLoading && !stats) {
        return (
            <div className="flex items-center justify-center py-20 text-slate-500">
                <Loader2 className="w-6 h-6 animate-spin mr-2" />
                Loading Overview...
            </div>
        );
    }

    return (
        <div className="space-y-6 animate-in fade-in duration-500">
            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-6">
                <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl shadow-sm hover:border-blue-500/30 transition-colors">
                    <div className="flex items-center gap-4">
                        <div className="w-12 h-12 rounded-xl bg-blue-500/10 flex items-center justify-center text-blue-500 border border-blue-500/20">
                            <FileText className="w-6 h-6" />
                        </div>
                        <div>
                            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Documents</div>
                            <div className="text-2xl font-bold text-slate-100 mt-0.5">{stats?.totalDocuments || 0}</div>
                        </div>
                    </div>
                </div>

                <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl shadow-sm hover:border-emerald-500/30 transition-colors">
                    <div className="flex items-center gap-4">
                        <div className="w-12 h-12 rounded-xl bg-emerald-500/10 flex items-center justify-center text-emerald-500 border border-emerald-500/20">
                            <Activity className="w-6 h-6" />
                        </div>
                        <div>
                            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Activity (24h)</div>
                            <div className="text-2xl font-bold text-slate-100 mt-0.5">{stats?.auditEvents24h || 0}</div>
                        </div>
                    </div>
                </div>

                <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl shadow-sm hover:border-amber-500/30 transition-colors">
                    <div className="flex items-center gap-4">
                        <div className="w-12 h-12 rounded-xl bg-amber-500/10 flex items-center justify-center text-amber-500 border border-amber-500/20">
                            <TrendingUp className="w-6 h-6" />
                        </div>
                        <div>
                            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Active Users</div>
                            <div className="text-2xl font-bold text-slate-100 mt-0.5">{stats?.topUsers.length || 0}</div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Top Users Table */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-sm">
                <div className="p-4 border-b border-slate-800 flex items-center gap-2 bg-slate-900/50">
                    <Users className="w-4 h-4 text-slate-400" />
                    <h2 className="font-semibold text-slate-200">Most Active Users</h2>
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full text-sm text-left">
                        <thead className="text-xs text-slate-400 uppercase bg-slate-800/50 border-b border-slate-700">
                            <tr>
                                <th className="px-6 py-3">User</th>
                                <th className="px-6 py-3 text-right">Event Count</th>
                            </tr>
                        </thead>
                        <tbody>
                            {!stats?.topUsers || stats.topUsers.length === 0 ? (
                                <tr>
                                    <td colSpan={2} className="px-6 py-8 text-center text-slate-500 italic">No activity recorded yet.</td>
                                </tr>
                            ) : (
                                stats.topUsers.map((user, i) => (
                                    <tr key={i} className="border-b border-slate-800/50 hover:bg-slate-800/30 transition-colors">
                                        <td className="px-6 py-4 font-medium text-slate-300">
                                            {user.username}
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <span className="bg-blue-500/10 text-blue-400 px-2.5 py-1 rounded-full border border-blue-500/20 font-semibold">
                                                {user.count}
                                            </span>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
