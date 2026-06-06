package backend.domain;

public enum DiceType {
    D6(6),
    D10(10),
    D20(20);

    private final int sides;

    DiceType(int sides) {
        this.sides = sides;
    }

    public int getSides() {
        return sides;
    }
}