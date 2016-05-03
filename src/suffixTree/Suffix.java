package suffixTree;

public final class Suffix {
	public Node origin;
    public int startIndex;
    public int endIndex;

    public Suffix(Node origin, int startIndex, int endIndex) {
        this.origin = origin;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    /**
     * @return True if the suffix represents an actual phrase end
     */
    public boolean isExplicit() {
        return startIndex > endIndex;
    }

    /**
     * @return True if the suffix is just a location along an edge, rather than
     * representing an actual phrase end
     */
    public boolean isImplicit() {
        return startIndex <= endIndex;
    }

    public int span() {
        return endIndex - startIndex;
    }
}
