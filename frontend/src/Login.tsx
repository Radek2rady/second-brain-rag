import React, { useState } from 'react';
import { Bot, Loader2 } from 'lucide-react';
import apiClient from './api/client';

interface LoginProps {
    onLoginSuccess: (token: string, roles: string[]) => void;
}

export default function Login({ onLoginSuccess }: LoginProps) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError('');

        try {
            const res = await apiClient.post('/api/auth/login', { username, password });
            const data = res.data;

            console.log("Login successful!");
            console.log("JWT Token:", data.token);
            console.log("Roles:", data.roles);
            onLoginSuccess(data.token, data.roles);
        } catch (err: any) {
            console.error(err);
            setError(err.response?.data?.message || 'Login failed');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex h-screen bg-slate-950 text-slate-200 justify-center items-center">
            <form onSubmit={handleLogin} className="w-full max-w-sm bg-slate-900 border border-slate-800 p-8 rounded-2xl shadow-lg">
                <div className="flex justify-center mb-6">
                    <div className="w-12 h-12 rounded-xl bg-blue-600 flex items-center justify-center shadow-sm">
                        <Bot className="w-7 h-7 text-white" />
                    </div>
                </div>
                <h2 className="text-2xl font-semibold text-center mb-6 text-slate-200">Login</h2>

                {error && (
                    <div className="bg-red-500/10 border border-red-500/50 text-red-500 p-3 rounded-lg mb-4 text-sm text-center">
                        {error}
                    </div>
                )}

                <div className="mb-4">
                    <label className="block text-sm font-medium text-slate-400 mb-2">Username</label>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="w-full px-4 py-2 bg-slate-800 border border-slate-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none"
                        placeholder="e.g. jan"
                        required
                    />
                </div>

                <div className="mb-6">
                    <label className="block text-sm font-medium text-slate-400 mb-2">Password</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full px-4 py-2 bg-slate-800 border border-slate-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none"
                        placeholder="••••••••"
                        required
                    />
                </div>

                <button
                    type="submit"
                    disabled={isLoading || !username || !password}
                    className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2.5 rounded-lg transition-colors flex justify-center items-center disabled:opacity-50"
                >
                    {isLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Log In'}
                </button>
            </form>
        </div>
    );
}
