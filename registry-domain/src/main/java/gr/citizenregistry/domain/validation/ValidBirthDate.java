package gr.citizenregistry.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Η ημερομηνία πρέπει να είναι της μορφής ΗΗ-ΜΜ-ΕΕΕΕ, υπαρκτή και όχι μελλοντική.
 * Η τιμή null θεωρείται έγκυρη (την υποχρεωτικότητα την ελέγχει το @NotBlank).
 */
@Documented
@Constraint(validatedBy = ValidBirthDateValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBirthDate {

    String message() default "Η ημερομηνία γέννησης πρέπει να είναι έγκυρη και της μορφής ΗΗ-ΜΜ-ΕΕΕΕ (π.χ. 12-11-2008)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
