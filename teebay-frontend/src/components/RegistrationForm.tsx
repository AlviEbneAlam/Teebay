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

export function RegisterForm() {
  const form = useForm({
    initialValues: {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
    },

    validate: {
      username: (value) =>
        value.length < 3 ? 'Username must be at least 3 characters' : null,
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
    <Center h="100vh">
        <Container size={420} my={40}>
            <Center>
                <Title>SIGN UP</Title>
            </Center>
        
        <Paper withBorder shadow="md" p={30} mt={30} radius="md">
            <form onSubmit={form.onSubmit(handleSubmit)}>
            <Grid>
                <Grid.Col span={6}>
                    <TextInput
                    label="First Name"
                    placeholder="First Name"
                    {...form.getInputProps('firstName')}
                    />
                </Grid.Col>
                <Grid.Col span={6}>
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
                <Grid.Col span={6}>
                    <TextInput
                    label="Email"
                    placeholder="Email"
                    {...form.getInputProps('email')}
                    />
                </Grid.Col>
                <Grid.Col span={6}>
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
                <Anchor href="/login" size="sm">
                    Sign in
                </Anchor>
            </Text>
            </form>
            
        </Paper>
        </Container>
    </Center>
   
    
  );
}

export default RegisterForm