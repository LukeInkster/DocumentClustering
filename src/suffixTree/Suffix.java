package suffixTree;

public final class Suffix {
	public Node origin;
    public int firstIndex;
    public int lastIndex;

    public Suffix() {

    }

    public Suffix(Node origin, int first, int last) {
        this.origin = origin;
        this.firstIndex = first;
        this.lastIndex = last;
    }

    /**
     * @return True if the node is represents an actual phrase end
     */
    public boolean isExplicit() {
        return firstIndex > lastIndex;
    }

    /**
     * @return True if the node is merely a location along an edge, rather than
     * representing an actual phrase end
     */
    public boolean isImplicit() {
        return firstIndex <= lastIndex;
    }

    public int span() {
        return lastIndex - firstIndex;
    }
}