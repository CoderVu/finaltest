package org.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CheckType {
    EQUALS("Equals"),
    CONTAINS("Contains"),
    GREATER_THAN("Greater Than"),
    LESS_THAN("Less Than"),
    GREATER_THAN_OR_EQUAL("Greater Than or Equal"),
    LESS_THAN_OR_EQUAL("Less Than or Equal"),
    NOT_EQUALS("Not Equals");

    private final String displayName;

    @Override
    public String toString() {
        return displayName;
    }
} 