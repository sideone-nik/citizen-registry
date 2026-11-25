package gr.citizenregistry.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidBirthDateValidator implements ConstraintValidator<ValidBirthDate, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || CitizenRules.isValidBirthDate(value);
    }
}
