package org.example.spring_practise.Enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SightCategory {
    MUSEUM("Музей"),
    MONUMENT("Памятник"),
    PARK("Парк"),
    THEATER("Театр"),
    RESTAURANT("Ресторан");

    private final String russianName;

    public static SightCategory fromRussianName(String text) {
        for (SightCategory cat : SightCategory.values()) {
            if (cat.russianName.equalsIgnoreCase(text)) {
                return cat;
            }
        }
        return null;
    }
}
