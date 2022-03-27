package commons;

public record Joker(
        String username,
        String jokerName
){

    @Override
    public String jokerName() {
        return jokerName;
    }

    @Override
    public String username() {
        return username;
    }
}

