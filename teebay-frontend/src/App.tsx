import {  MantineProvider } from '@mantine/core';
import {RegisterForm} from './components/RegistrationForm';
import '@mantine/core/styles.css';

function App() {
  return (

    <MantineProvider>
       <RegisterForm/>
    </MantineProvider>
  );
}

export default App
