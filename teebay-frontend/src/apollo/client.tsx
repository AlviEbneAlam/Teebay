import { ApolloClient, InMemoryCache, ApolloProvider } from '@apollo/client';

const client = new ApolloClient({
  uri: 'http://localhost:8082/graphql',
  cache: new InMemoryCache(),
//   headers: {
//     authorization: `Bearer ${localStorage.getItem('access_token') || ''}`
//   }
});

export const MyApolloProvider = ({ children }: { children: React.ReactNode }) => (
  <ApolloProvider client={client}>{children}</ApolloProvider>
);