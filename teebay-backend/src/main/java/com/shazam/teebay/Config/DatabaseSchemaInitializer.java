package com.shazam.teebay.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DatabaseSchemaInitializer implements CommandLineRunner {
    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String defaultSchema;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);

    private final DataSource dataSource;

    public DatabaseSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Applying custom database schema changes...");

        String[] sqlStatements = {
                "CREATE EXTENSION IF NOT EXISTS btree_gist;",

                "DO $$ BEGIN " +
                        "IF NOT EXISTS (" +
                        "SELECT 1 FROM information_schema.columns " +
                        "WHERE table_schema = '"+defaultSchema+"' AND table_name = 'rent_bookings' AND column_name = 'period') " +
                        "THEN EXECUTE 'ALTER TABLE "+defaultSchema+".rent_bookings ADD COLUMN period tsrange GENERATED ALWAYS AS (tsrange(rent_start_time, rent_end_time, ''[]'')) STORED'; " +
                        "END IF; END $$;",

                "DO $$ BEGIN " +
                        "IF NOT EXISTS (" +
                        "SELECT 1 FROM pg_class c " +
                        "JOIN pg_namespace n ON n.oid = c.relnamespace " +
                        "WHERE c.relname = 'idx_rent_bookings_period' AND n.nspname = '"+defaultSchema+"') " +
                        "THEN EXECUTE 'CREATE INDEX idx_rent_bookings_period ON "+defaultSchema+".rent_bookings USING GIST (product_id, period)'; " +
                        "END IF; END $$;",

                "DO $$ BEGIN " +
                        "IF NOT EXISTS (" +
                        "SELECT 1 FROM pg_constraint WHERE conname = 'no_overlapping_bookings') " +
                        "THEN EXECUTE 'ALTER TABLE "+defaultSchema+".rent_bookings ADD CONSTRAINT no_overlapping_bookings EXCLUDE USING GIST (product_id WITH =, period WITH &&)'; " +
                        "END IF; END $$;"
        };

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                for (String sql : sqlStatements) {
                    logger.info("Executing SQL: {}", sql);
                    statement.execute(sql);
                }
                connection.commit();
                logger.info("Custom database schema changes applied successfully.");
            } catch (SQLException e) {
                connection.rollback();
                logger.error("Failed to apply custom database schema changes. Rolling back transaction.", e);
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Failed to get database connection or execute statements.", e);
            throw e;
        }
    }
}
