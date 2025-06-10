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

export function RegistrationForm() {
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
    },
  });

  const handleSubmit = (values: typeof form.values) => {
    console.log('Submitted values:', values);
    // TODO: Send data to your backend API
  };

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