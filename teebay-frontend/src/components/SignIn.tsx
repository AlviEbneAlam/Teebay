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
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@apollo/client';
import { LOGIN_USER } from '../graphql/mutations';
import { useAuth } from '../auth/useAuth';  

export function SignIn() {

  const navigate = useNavigate();
  const [loginMutation] = useMutation(LOGIN_USER);
  const { login } = useAuth();

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

  const handleSubmit = async (values: typeof form.values) => {
    try {
      const { data } = await loginMutation({
        variables: {
          email: values.email,
          password: values.password
        }
      });

      if (data?.login?.jwtToken) {
        login(data.login.jwtToken); 
        console.log('Successful login');
        navigate('/LoggedInMessage');
      } else {
        console.error('Login failed:', data?.login?.message);
      }
    } catch (err) {
      console.error('Login error:', err);
    }
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
              {...form.getInputProps('password')} 
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