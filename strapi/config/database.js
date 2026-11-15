module.exports = ({ env }) => {
  const cliente = env('DATABASE_CLIENT', 'postgres');

  if (cliente === 'sqlite') {
    return {
      connection: {
        client: 'sqlite',
        connection: {
          filename: env('DATABASE_FILENAME', '.tmp/datos.db'),
        },
        useNullAsDefault: true,
      },
    };
  }

  return {
    connection: {
      client: 'postgres',
      connection: {
        host: env('DATABASE_HOST'),
        port: env.int('DATABASE_PORT', 5432),
        database: env('DATABASE_NAME'),
        user: env('DATABASE_USERNAME'),
        password: env('DATABASE_PASSWORD'),
        ssl: env.bool('DATABASE_SSL', false) && { rejectUnauthorized: false },
      },
    },
  };
};
