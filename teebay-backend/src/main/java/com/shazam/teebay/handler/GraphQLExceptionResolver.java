package com.shazam.teebay.handler;

import com.shazam.teebay.exception.GraphQLDataProcessingException;
import com.shazam.teebay.exception.GraphQLValidationException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        if (ex instanceof GraphQLValidationException || ex instanceof GraphQLDataProcessingException) {
            return GraphqlErrorBuilder.newError(env)
                    .message(ex.getMessage())
                    .errorType(graphql.ErrorType.DataFetchingException)
                    .build();
        }

        // fallback for any other unhandled exception
        return GraphqlErrorBuilder.newError(env)
                .message("Something went wrong. Please try again later.")
                .errorType(graphql.ErrorType.DataFetchingException)
                .build();
    }
}