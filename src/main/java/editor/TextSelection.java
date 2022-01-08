package editor;

public class TextSelection {
    private int start;
    private int end;

    public TextSelection(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "TextSelection{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
