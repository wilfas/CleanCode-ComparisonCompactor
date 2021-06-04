import junit.framework.Assert;

@SuppressWarnings("deprecation")
public class ComparisonCompactor {

	private static final String ELLIPSIS = "...";
	private static final String DELTA_END = "]";
	private static final String DELTA_START = "[";

	private int contextLength;
	private String expected;
	private String actual;
	private int prefixLength;
	private int suffixLength;

	public ComparisonCompactor(int contextLength, String expected, String actual) {
		this.contextLength = contextLength;
		this.expected = expected;
		this.actual = actual;
	}

	public String formatCompactedComparison(String message) {
		String compactExpected = expected;
		String compactActual = actual;
		if (shouldBeCompacted()) {
			findCommonPrefixAndSuffix();
			compactExpected = compact(expected);
			compactActual = compact(actual);
		}	
		return Assert.format(message, compactExpected, compactActual);
	}

	private boolean shouldBeCompacted() {
		return !shouldNotBeCompacted();
	}

	private boolean shouldNotBeCompacted() {		
		return expected == null || actual == null || expected.equals(actual);
	}

	private void findCommonPrefixAndSuffix() {
		findCommonPrefix();
		suffixLength = 0;
		for (; !suffixOverlapsPrefix(suffixLength); suffixLength++) {
			if (charFromEnd(expected, suffixLength) != charFromEnd(actual, suffixLength)) 
				break;
		}
	}

	private void findCommonPrefix() {
		prefixLength = 0;
		int end = Math.min(expected.length(), actual.length());
		for (; prefixLength < end; prefixLength++)
			if (expected.charAt(prefixLength) != actual.charAt(prefixLength))
				break;
	}

	private boolean suffixOverlapsPrefix(int suffixLength) {
		return 
			actual.length() - suffixLength <= prefixLength ||
			expected.length() - suffixLength <= prefixLength;
	}

	private char charFromEnd(String s, int i) {
		return s.charAt(s.length() - i - 1);
	}

	private String compact(String source) {
		return new StringBuilder()
			.append(computeCommonPrefix())
			.append(DELTA_START) 
			.append(source.substring(prefixLength, source.length() - suffixLength))
			.append(DELTA_END)
			.append(computeCommonSuffix())
			.toString();
	}

	private String computeCommonPrefix() {
		return (prefixLength > contextLength ? ELLIPSIS : "")
			+ expected.substring(Math.max(0, prefixLength - contextLength), prefixLength);
	}

	private String computeCommonSuffix() {
		int end = Math.min(
			expected.length() - suffixLength + contextLength, 
			expected.length()
			);
		return expected.substring(expected.length() - suffixLength, end)
				+ (expected.length() - suffixLength < expected.length() - contextLength ? ELLIPSIS : "");
	}
}
