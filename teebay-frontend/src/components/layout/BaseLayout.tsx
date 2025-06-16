// components/layout/BaseLayout.tsx
import { AppShell, Button, Group, Container, Text } from '@mantine/core';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../auth/useAuth';

type Props = {
  children: React.ReactNode;
};

const BaseLayout = ({ children }: Props) => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <AppShell header={{ height: 60 }} padding="md">
      <AppShell.Header>
        <Container size="xl" style={{ height: '100%' }}>
          <Group justify="space-between" align="center" style={{ height: '100%' }}>
            <Text fw={700}>My App</Text>
            <Group>
              <Button
                variant={location.pathname === '/allProducts' ? 'filled' : 'light'}
                onClick={() => navigate('/allProducts')}
              >
                All Products
              </Button>
              <Button
                variant={location.pathname === '/myProducts' ? 'filled' : 'light'}
                onClick={() => navigate('/myProducts')}
              >
                My Products
              </Button>
               <Button
                variant={location.pathname === '/activity' ? 'filled' : 'light'}
                onClick={() => navigate('/MyActivity')}
              >
                Activity
              </Button>
            </Group>
            <Button color="red" onClick={handleLogout}>
              Logout
            </Button>
          </Group>
        </Container>
      </AppShell.Header>
      <AppShell.Main>
        <Container>{children}</Container>
      </AppShell.Main>
    </AppShell>
  );
};

export default BaseLayout;
