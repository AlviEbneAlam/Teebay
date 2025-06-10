import {  MantineProvider } from '@mantine/core';
import {RegistrationForm} from './components/RegistrationForm';
import {SignIn} from './components/SignIn';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import '@mantine/core/styles.css';

function App() {
  return (

    <MantineProvider>
       <Router>
          <Routes>
            <Route path="/" element={<SignIn />} />
            <Route path="/register" element={<RegistrationForm />} />
          </Routes>
    </Router>
    </MantineProvider>
  );
}

export default App
