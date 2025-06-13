import {  MantineProvider } from '@mantine/core';
import {RegistrationForm} from './components/RegistrationForm';
import {SignIn} from './components/SignIn';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import { LoggedInMessage } from './components/LoggedInMessage';
import {StepperForm} from './components/Form/StepperForm'
import '@mantine/core/styles.css';
import BaseLayout from './components/layout/BaseLayout';

function App() {
  return (
    <MantineProvider>
      <Router>
        <AuthProvider>
          <Routes>
            <Route path="/" element={<SignIn />} />
            <Route path="/register" element={<RegistrationForm />} />
            <Route
              path="/LoggedInMessage"
              element={
                <ProtectedRoute>
                  <BaseLayout>
                    <LoggedInMessage />
                  </BaseLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/StepperForm"
              element={
                <ProtectedRoute>
                  <BaseLayout>
                    <StepperForm />
                  </BaseLayout>
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </Router>
    </MantineProvider>
  );
}


export default App
