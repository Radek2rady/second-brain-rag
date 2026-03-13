import { useState, useEffect } from 'react';

export function useAuth() {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [isAdmin, setIsAdmin] = useState<boolean>(false);
  const [currentUsername, setCurrentUsername] = useState<string>('');

  useEffect(() => {
    if (token) {
      // Decode JWT payload to get roles and sub (username)
      try {
        const payloadBase64 = token.split('.')[1];
        const decodedPayload = JSON.parse(atob(payloadBase64));
        const roles: string[] = decodedPayload.roles || [];
        // Support both ROLE_ADMIN and ADMIN depending on Spring Boot configuration
        setIsAdmin(roles.includes('ROLE_ADMIN') || roles.includes('ADMIN'));
        
        if (decodedPayload.sub) {
          setCurrentUsername(decodedPayload.sub);
        }
      } catch (e) {
        console.error("Failed to parse token payload", e);
        setIsAdmin(false);
      }
    } else {
      setIsAdmin(false);
      setCurrentUsername('');
    }
  }, [token]);

  const login = (newToken: string, roles: string[]) => {
    setToken(newToken);
    localStorage.setItem('token', newToken);
    localStorage.setItem('rag_roles', JSON.stringify(roles));
  };

  const logout = () => {
    setToken(null);
    setIsAdmin(false);
    setCurrentUsername('');
    localStorage.removeItem('token');
    localStorage.removeItem('rag_roles');
  };

  return { token, isAdmin, currentUsername, login, logout };
}
