package xyz.suchdoge.webapi.service.validator;

import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ModelValidatorService {
    private final Validator validator;

    public ModelValidatorService(Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T obj) {
        List<String> errorMessages = getErrorMessages(obj);

        if (errorMessages.size() > 0) {
            errorMessages.sort(String::compareTo);

            errorMessages.forEach(errorMessage -> {
                throw new RuntimeException(errorMessage);
            });
        }
    }

    private <T> List<String> getErrorMessages(T obj) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj);

        List<String> errorMessages = constraintViolations
                .stream()
                .map(constraintViolation -> constraintViolation.getMessage())
                .collect(Collectors.toList());

        return errorMessages;
    }
}
