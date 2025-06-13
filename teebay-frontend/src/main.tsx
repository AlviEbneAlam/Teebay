import {  MantineProvider } from '@mantine/core';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { MyApolloProvider } from './apollo/client';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MyApolloProvider>
      <MantineProvider >
           <App/>
      </MantineProvider>
    </MyApolloProvider>
  </StrictMode>,
)
