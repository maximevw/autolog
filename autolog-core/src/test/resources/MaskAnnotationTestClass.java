import com.github.maximevw.autolog.core.annotations.Mask;

public class MaskAnnotationTestClass {

	public void maskOnInvalidType(@Mask final Object argObj, @Mask final String argStr, @Mask final int argInt,
								  final boolean argBool) {
		// Method for testing purpose only.
	}

	public void maskWithInvalidPreservedCharactersExpression(final String argStrNotMasked,
		@Mask(preservedCharacters = "0:1,") final String argStrInvalidMask,
		@Mask(preservedCharacters = "0:1, [-]") final String argStrValidMask) {
		// Method for testing purpose only.
	}

	public void maskWithInvalidFixedLength(final String argStrNotMasked,
		@Mask(preservedCharacters = "0:1", fixedLength = 5) final String argStrInvalidFixedLength,
		@Mask(preservedCharacters = "0:0", fixedLength = 5) final String argStrValidFixedLength,
		@Mask(fixedLength = 5) final String otherArgStrValidFixedLength) {
		// Method for testing purpose only.
	}

}