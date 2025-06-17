import {
  TextInput,
  PasswordInput,
  Paper,
  Group,
  Button,
  Container,
  Title,
  Center, Grid, Anchor, Text
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { Link } from 'react-router-dom';
import { useMutation } from '@apollo/client';
import { REGISTER_USER } from '../graphql/mutations';
import { useNavigate } from 'react-router-dom';

export function RegistrationForm() {

  const [registerUser] = useMutation(REGISTER_USER);
  const navigate = useNavigate();

  const form = useForm({
    initialValues: {
      firstName:'',
      lastName:'',
      address:'',
      email: '',
      phoneNumber:'',
      password: '',
      confirmPassword: '',
    },

    validate: {
      email: (value) =>
        /^\S+@\S+$/.test(value) ? null : 'Invalid email address',
      password: (value) =>
        value.length < 8 ? 'Password must be at least 8 characters' : null,
      confirmPassword: (value, values) =>
        value !== values.password ? 'Passwords do not match' : null,
      phoneNumber: (value) =>
      /^01[0-9]{9}$/.test(value)
        ? null
        : 'Phone number must be 11 digits and start with 01',
    },
  });

  const handleSubmit = (values: typeof form.values) => {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { confirmPassword, ...input } = values;

    registerUser({ variables: { input } })
      .then((response) => {
        const { statusCode } = response.data.register;

        if (statusCode === "0") {
          alert('User registered successfully');
        } else {
          alert('Failed to register user');
        }
      })
      .catch((err) => {
        console.error('Registration error:', err);
        alert('Failed to register user');
      })
      .finally(() => {
        // This will always run regardless of success or error
        setTimeout(() => navigate('/'), 300);
      });
    }


  return (
    <Center >
        <Container size={420} my={40}>
            <Center>
                <Title>SIGN UP</Title>
            </Center>
        
        <Paper withBorder shadow="md" p={30} mt={10} radius="md">
            <form onSubmit={form.onSubmit(handleSubmit)}>
            <Grid>
                <Grid.Col span={{ base: 12, sm: 6 }}>
                    <TextInput
                    label="First Name"
                    placeholder="First Name"
                    {...form.getInputProps('firstName')}
                    />
                </Grid.Col>
                <Grid.Col span={{ base: 12, sm: 6 }}>
                    <TextInput
                    label="Last Name"
                    placeholder="Last Name"
                    {...form.getInputProps('lastName')}
                    />
                </Grid.Col>
            </Grid>
            <TextInput
                label="Address"
                placeholder="Address"
                mt="md"
                {...form.getInputProps('address')}
            />
            <Grid>
                <Grid.Col span={{ base: 12, sm: 6 }}>
                    <TextInput
                    label="Email"
                    placeholder="Email"
                    {...form.getInputProps('email')}
                    />
                </Grid.Col>
                <Grid.Col span={{ base: 12, sm: 6 }}>
                    <TextInput
                    label="Phone Number"
                    placeholder="Phone Number"
                    {...form.getInputProps('phoneNumber')}
                    />
                </Grid.Col>
            </Grid> 
            <PasswordInput
                label="Password"
                placeholder="Your password"
                mt="md"
                {...form.getInputProps('password')}
            />
            <PasswordInput
                label="Confirm Password"
                placeholder="Repeat password"
                mt="md"
                {...form.getInputProps('confirmPassword')}
            />
            <Group justify="space-between" mt="lg">
                <Button type="submit" fullWidth>
                Register
                </Button>
            </Group>
            <Text ta="center" mt="md" size="sm">
                Already have an account?{' '}
                <Anchor component={Link} to="/"  size="sm">
                    Sign In
                </Anchor>
            </Text>
            </form>
            
        </Paper>
        </Container>
    </Center>
   
    
  );
}

export default RegistrationForm