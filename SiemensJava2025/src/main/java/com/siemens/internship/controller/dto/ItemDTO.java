package com.siemens.internship.controller.dto;

import com.siemens.internship.annotation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDTO {

    private Long id;

    @NotBlank(message = "Name is mandatory!")
    private String name;

    @NotBlank(message = "Description is mandatory!")
    private String description;

    @NotBlank(message = "Status is mandatory!")
    private String status;

    @NotNull(message = "Email is mandatory!")
    @Email(regexp = "^([a-zA-Z]+[0-9]*[-_.]*)+[a-zA-Z]+[0-9]*" + // [email name]
            "@([a-zA-Z]+[0-9]*[.-]*)+[a-zA-Z]+[0-9]*" + // [app]
            "[.][a-zA-Z]{2,4}", // [domain]
            message = "Email is invalid!")
    @UniqueEmail(message = "This email is already taken!")
    private String email;
}