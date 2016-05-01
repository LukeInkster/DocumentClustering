package suffixTree;


// Represents a suffix. Used while building the suffix tree.
public final class Suffix {
	public Node origin;
    public int firstIndex;
    public int lastIndex;

    public Suffix() {}
    public Suffix(Node origin, int first, int last) {
        this.origin = origin;
        this.firstIndex = first;
        this.lastIndex = last;
    }

    public boolean isExplicit() {
        return firstIndex > lastIndex;
    }

    public boolean isImplicit() {
        return firstIndex <= lastIndex;
    }

    public int span() {
        return lastIndex - firstIndex;
    }
}
