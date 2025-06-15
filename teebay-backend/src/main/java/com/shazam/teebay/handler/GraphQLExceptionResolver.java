package com.shazam.teebay.handler;

import com.shazam.teebay.exception.GraphQLDataProcessingException;
import com.shazam.teebay.exception.GraphQLValidationException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {


    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {


        log.info("Exception during data fetching for field '{}': {}", env.getField().getName(), ex.getMessage(), ex);


        if (ex instanceof ConstraintViolationException violationEx) {
            String message = violationEx.getConstraintViolations().stream()
                    .map(this::formatViolationMessage)
                    .collect(Collectors.joining("; "));

            log.info("Validation failed: {}", message);

            return GraphqlErrorBuilder.newError(env)
                    .message(message)
                    .errorType(graphql.ErrorType.ValidationError)
                    .build();
        }

        // Optional: custom application exceptions
        if (ex instanceof GraphQLValidationException || ex instanceof GraphQLDataProcessingException) {
            return GraphqlErrorBuilder.newError(env)
                    .message(ex.getMessage())
                    .errorType(graphql.ErrorType.DataFetchingException)
                    .build();
        }

        // Fallback for all other unhandled errors
        return GraphqlErrorBuilder.newError(env)
                .message("Something went wrong. Please try again later.")
                .errorType(graphql.ErrorType.DataFetchingException)
                .build();
    }

    private String formatViolationMessage(ConstraintViolation<?> violation) {
        return String.format("%s.%s: %s",
                violation.getRootBeanClass().getSimpleName(),
                violation.getPropertyPath(),
                violation.getMessage());
    }
}