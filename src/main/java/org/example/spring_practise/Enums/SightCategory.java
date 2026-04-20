package org.example.spring_practise.Enums;

@Getter
@RequiredArgsConstructor
public enum SightCategory {
    MUSEUM("Музей"),
    MONUMENT("Памятник"),
    PARK("Парк"),
    THEATER("Театр"),
    RESTAURANT("Ресторан");
    private final String name;
    @Override
    public String toString() {
        return name;
    }
}
