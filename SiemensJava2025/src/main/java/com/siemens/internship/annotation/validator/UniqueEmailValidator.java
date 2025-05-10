package com.siemens.internship.annotation.validator;

import com.siemens.internship.annotation.UniqueEmail;
import com.siemens.internship.exception.ObjectNotFoundException;
import com.siemens.internship.service.ItemService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final ItemService itemService;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        try {
            itemService.findByEmail(email);
            return false;
        } catch (ObjectNotFoundException e) {
            return true;
        }
    }
}
