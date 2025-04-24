package com.danya.mdm.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final Validator validator;

    public <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation errors: ");
            violations.forEach(v ->
                    errorMessage.append(v.getPropertyPath())
                            .append(" – ")
                            .append(v.getMessage())
                            .append("; ")
            );
            log.warn(errorMessage.toString());
            throw new ConstraintViolationException(errorMessage.toString(), violations);
        }
    }
}