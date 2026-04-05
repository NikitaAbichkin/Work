import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import LoadingSpinner from './LoadingSpinner';

export default function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
