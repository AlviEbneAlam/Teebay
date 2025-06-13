import { Navigate } from 'react-router-dom';
import {useAuth} from '../auth/useAuth';
import type { JSX } from 'react';

type Props = {
  children: JSX.Element;
};

const ProtectedRoute = ({ children }: Props) => {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default ProtectedRoute;