package commons;

public record Joker(
        String username,
        String jokerName
) {
    public enum JokerStatus {
        AVAILABLE,
        USED_HOT,
        USED
    }
}

