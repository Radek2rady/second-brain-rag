import React, { useState, useEffect } from 'react';
import { Loader2, Shield, User } from 'lucide-react';

interface UserDto {
    id: number;
    username: string;
    role: string;
    departmentId: string | null;
}

interface UserListProps {
    token: string;
    currentUsername: string;
}

export default function UserList({ token, currentUsername }: UserListProps) {
    const [users, setUsers] = useState<UserDto[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [updatingId, setUpdatingId] = useState<number | null>(null);

    const fetchUsers = async () => {
        setIsLoading(true);
        try {
            const res = await fetch('http://localhost:8080/api/users', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (res.ok) {
                setUsers(await res.json());
            }
        } catch (e) {
            console.error(e);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, [token]);

    const handleRoleChange = async (userId: number, newRole: string) => {
        setUpdatingId(userId);
        try {
            const res = await fetch(`http://localhost:8080/api/users/${userId}/role`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ role: newRole })
            });

            if (res.ok) {
                const updatedUser = await res.json();
                setUsers(users.map(u => u.id === userId ? updatedUser : u));
            } else {
                alert('Error changing role.');
            }
        } catch (e) {
            console.error(e);
        } finally {
            setUpdatingId(null);
        }
    };

    return (
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden mt-6 shadow-sm">
            <div className="p-4 border-b border-slate-800 flex justify-between items-center bg-slate-900/50">
                <h2 className="font-semibold text-slate-200">User Management</h2>
                <div className="text-sm text-slate-400">
                    Users: {users.length}
                </div>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-sm text-left">
                    <thead className="text-xs text-slate-400 uppercase bg-slate-800/50 border-b border-slate-700">
                        <tr>
                            <th className="px-4 py-3">User</th>
                            <th className="px-4 py-3">Department</th>
                            <th className="px-4 py-3">Role</th>
                            <th className="px-4 py-3 text-right">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading && users.length === 0 ? (
                            <tr>
                                <td colSpan={4} className="px-4 py-8 text-center text-slate-500">
                                    <Loader2 className="w-5 h-5 animate-spin mx-auto mb-2" />
                                    Loading users...
                                </td>
                            </tr>
                        ) : (
                            users.map((user) => (
                                <tr key={user.id} className="border-b border-slate-800/50 hover:bg-slate-800/30 transition-colors">
                                    <td className="px-4 py-3 flex items-center gap-2">
                                        <div className="w-6 h-6 rounded-full bg-slate-800 flex items-center justify-center text-slate-400">
                                            <User className="w-3.5 h-3.5" />
                                        </div>
                                        <span className="font-medium text-slate-300">{user.username}</span>
                                        {user.username === currentUsername && (
                                            <span className="text-[10px] bg-blue-500/20 text-blue-400 px-1.5 py-0.5 rounded ml-1 border border-blue-500/30">
                                                YOU
                                            </span>
                                        )}
                                    </td>
                                    <td className="px-4 py-3 text-slate-400">
                                        {user.departmentId || '-'}
                                    </td>
                                    <td className="px-4 py-3">
                                        <div className="flex items-center gap-1.5">
                                            {user.role === 'ADMIN' ? (
                                                <Shield className="w-3.5 h-3.5 text-blue-400" />
                                            ) : (
                                                <User className="w-3.5 h-3.5 text-slate-500" />
                                            )}
                                            <span className={user.role === 'ADMIN' ? 'text-blue-400 font-medium' : 'text-slate-400'}>
                                                {user.role}
                                            </span>
                                        </div>
                                    </td>
                                    <td className="px-4 py-3 text-right">
                                        <select
                                            disabled={updatingId === user.id}
                                            value={user.role}
                                            onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                            className="bg-slate-800 border border-slate-700 text-slate-200 text-xs rounded focus:ring-blue-500 focus:border-blue-500 block w-full p-2 py-1.5 cursor-pointer disabled:opacity-50 appearance-none"
                                        >
                                            <option value="USER">USER</option>
                                            <option value="ADMIN">ADMIN</option>
                                        </select>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
