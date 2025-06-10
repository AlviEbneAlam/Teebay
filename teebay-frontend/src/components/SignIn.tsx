import {
  TextInput,
  PasswordInput,
  Paper,
  Group,
  Button,
  Container,
  Title,
  Center, Anchor, Text
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { Link } from 'react-router-dom';

export function SignIn() {
   const form = useForm({
    initialValues: {
      email: '',
      password: ''
    },

    validate: {
      email: (value) =>
        /^\S+@\S+$/.test(value) ? null : 'Invalid email address',
      password: (value) =>
        value.length < 8 ? 'Password must be at least 8 characters' : null
    },
  });

  const handleSubmit = (values: typeof form.values) => {
    console.log('Submitted values:', values);
    // TODO: Send data to your backend API
  };

  return (
    <Center h="100vh">
        <Container size={420} my={40}>
            <Center>
                <Title>SIGN IN</Title>
            </Center>
        
        <Paper withBorder shadow="md" p={30} mt={10} radius="md">
            <form onSubmit={form.onSubmit(handleSubmit)}>
           <TextInput
                    label="Email"
                    placeholder="Email"
                    w="100%"     
                    {...form.getInputProps('email')}
            />
            <PasswordInput
                label="Password"
                placeholder="Password"
                mt="md"
                {...form.getInputProps('confirmPassword')}
            />
            <Group justify="space-between" mt="lg">
                <Button type="submit" fullWidth>
                    Login                
                </Button>
            </Group>
            <Text ta="center" mt="md" size="sm">
                Don't have an account?{' '}
                <Anchor component={Link} to="/register"  size="sm">
                    Sign Up
                </Anchor>
            </Text>
            </form>
            
        </Paper>
        </Container>
    </Center>
   
    
  );
}

export default SignIn