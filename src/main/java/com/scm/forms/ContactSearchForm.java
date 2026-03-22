package com.scm.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ContactSearchForm {

    @NotBlank(message = "Search field is required")
    private String field;

    @NotBlank(message = "Search value is required")
    @Size(min = 1, max = 100, message = "Search value must be between 1 and 100 characters")
    private String value;
}