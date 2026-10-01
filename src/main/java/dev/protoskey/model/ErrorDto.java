package dev.protoskey.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {
    private String message;
    private Map<String, String> fieldErrors;

    public ErrorDto(String message) {
        this(message, Map.of());
    }
}
