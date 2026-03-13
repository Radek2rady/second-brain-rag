import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { setNavigate } from '../api/client';

export function NavigationSetter() {
  const navigate = useNavigate();

  useEffect(() => {
    setNavigate(navigate);
  }, [navigate]);

  return null;
}
