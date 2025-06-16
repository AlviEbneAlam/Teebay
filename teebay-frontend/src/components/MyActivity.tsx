import {
  Container,
  Title,
  Tabs,
  Text
} from '@mantine/core';
import React from 'react';

export const MyActivity: React.FC = () => {
  return (
    <Container size="md" mt="lg">
      <Title order={2} mb="lg" ta="center">
        My Activity
      </Title>

      <Tabs defaultValue="bought" keepMounted={false}>
        <Tabs.List>
          <Tabs.Tab value="bought">Bought</Tabs.Tab>
          <Tabs.Tab value="sold">Sold</Tabs.Tab>
          <Tabs.Tab value="borrowed">Borrowed</Tabs.Tab>
          <Tabs.Tab value="lent">Lent</Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="bought" pt="md">
          {/* TODO: Replace with real data */}
          <Text>Bought items will be listed here</Text>
        </Tabs.Panel>

        <Tabs.Panel value="sold" pt="md">
          <Text>Sold items will be listed here</Text>
        </Tabs.Panel>

        <Tabs.Panel value="borrowed" pt="md">
          <Text>Borrowed items will be listed here</Text>
        </Tabs.Panel>

        <Tabs.Panel value="lent" pt="md">
          <Text>Lent items will be listed here</Text>
        </Tabs.Panel>
      </Tabs>
    </Container>
  );
};
