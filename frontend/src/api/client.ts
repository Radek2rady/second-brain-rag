import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

let navigate: (path: string) => void = (path) => {
  window.location.href = path;
};

export const setNavigate = (nav: (path: string) => void) => {
  navigate = nav;
};

// Response Interceptor for Error Handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Clear token and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('rag_roles');
      
      // Use react-router-dom navigate if available, otherwise fallback to reload/href
      navigate('/login');
    }
    return Promise.reject(error);
  }
);

export default apiClient;
