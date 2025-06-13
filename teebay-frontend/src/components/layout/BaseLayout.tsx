// components/layout/BaseLayout.tsx
import { AppShell, Button, Group, Container, Text } from '@mantine/core';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/useAuth';

type Props = {
  children: React.ReactNode;
};

const BaseLayout = ({ children }: Props) => {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <AppShell
      header={{ height: 60 }}
      padding="md"
    >
       <AppShell.Header>
        <Container size="xl" style={{ height: '100%' }}>
          <Group justify="space-between" align="center" style={{ height: '100%' }}>
            <Text fw={700}>My App</Text>
            <Button onClick={handleLogout}>Logout</Button>
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