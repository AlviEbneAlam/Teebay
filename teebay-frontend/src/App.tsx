import {  MantineProvider } from '@mantine/core';
import {RegistrationForm} from './components/RegistrationForm';
import {SignIn} from './components/SignIn';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import { MyProducts } from './components/MyProducts';
import { AllProducts } from './components/AllProducts';
import { ProductDetails } from './components/ProductDetails';
import {StepperForm} from './components/Form/StepperForm';
import {EditProductForm} from './components/Form/EditProductForm';
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
              path="/edit-product/:id"
              element={
                <ProtectedRoute>
                  <BaseLayout>
                    <EditProductForm />
                  </BaseLayout>
                </ProtectedRoute>
              }
            />
             <Route
              path="/product/:id"
              element={
                <ProtectedRoute>
                  <BaseLayout>
                    <ProductDetails />
                  </BaseLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/MyProducts"
              element={
                <ProtectedRoute>
                  <BaseLayout>
                    <MyProducts />
                  </BaseLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/AllProducts"
              element={
                <ProtectedRoute>
                  <BaseLayout>
                    <AllProducts />
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
