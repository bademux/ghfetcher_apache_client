package com.github.bademux.ghfetcher.controller;

import com.github.bademux.ghfetcher.client.RateLimitException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.SpringAdviceTrait;

import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.SERVICE_UNAVAILABLE;

@ControllerAdvice
class ExceptionHandling implements ProblemHandling, RateLimiter {
}


interface RateLimiter extends SpringAdviceTrait {

    @ExceptionHandler
    default ResponseEntity<Problem> handleRateLimiter(ResourceAccessException exception, NativeWebRequest request) {
        if (isRateLimitException(exception)) {
            return create(SERVICE_UNAVAILABLE, exception, request);
        }
        return create(INTERNAL_SERVER_ERROR, exception, request);
    }

    private boolean isRateLimitException(ResourceAccessException exception) {
        return ofNullable(exception)
                .map(Throwable::getCause)
                .map(Throwable::getCause)
                .map(Object::getClass)
                .filter(RateLimitException.class::isAssignableFrom)
                .isPresent();
    }

}
