package util;

public class Counter {

    private int value;

    public Counter() {
        this.value = 0;
    }

    public Counter(int value) {
        this.value = value;
    }

    public void increment() {
        value += 1;
    }

    public int getValue() {
        return value;
    }
}
