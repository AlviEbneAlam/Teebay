import React from 'react';
import { Container, Center, Paper, Text, Title, Button } from '@mantine/core';
import { Link } from 'react-router-dom';

export const LoggedInMessage: React.FC = () => {
  return (
    <div style={{ position: 'relative', height: '100vh' }}>
      <Center h="100%">
        <Container size={420}>
          <Paper shadow="md" p="xl" radius="md" withBorder>
            <Title order={3} ta="center" mb="md">
              Welcome!
            </Title>
            <Text ta="center" size="md" color="dimmed">
              You are a logged-in user.
            </Text>
          </Paper>
        </Container>
      </Center>

      <Button
        component={Link}
        to="/StepperForm"  
        style={{
          position: 'absolute',
          bottom: 20,
          right: 20,
        }}
      >
        Add Product
      </Button>
    </div>

  );
};