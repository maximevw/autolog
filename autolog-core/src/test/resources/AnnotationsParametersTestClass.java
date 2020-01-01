import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInput;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput;

public class AnnotationsParametersTestClass {

	@AutoLogMethodInOut(
		inputMessageTemplate = "{}", outputMessageTemplate = "{}", voidOutputMessageTemplate = ""
	)
	public void inOutWrongPlaceholders() {
		// Method for testing purpose only.
	}

	@AutoLogMethodInput(
		messageTemplate = "{}",
		prettify = {AutoLogMethodInOut.ALL_DATA, AutoLogMethodInOut.INPUT_DATA, AutoLogMethodInOut.OUTPUT_DATA}
	)
	@AutoLogMethodOutput(
		messageTemplate = "{}", voidOutputMessageTemplate = ""
	)
	public void inputAndOutputAnnotatedMethod() {
		// Method for testing purpose only.
	}

	@AutoLogMethodInOut(
		inputMessageTemplate = "{}{}", outputMessageTemplate = "{}{}", voidOutputMessageTemplate = "{}",
		prettify = {"test"}, structuredMessage = true
	)
	public void inOutWithIgnoredParameters() {
		// Method for testing purpose only.
	}

	@AutoLogMethodInOut(
		structuredMessage = true
	)
	public void inOutStructuredMessageWithoutIgnoredParameters() {
		// Method for testing purpose only. Should not generate any warning.
	}

	@AutoLogMethodInput(
		messageTemplate = "{}{}", prettify = {"test"}, structuredMessage = true
	)
	public void inputWithIgnoredParameters() {
		// Method for testing purpose only.
	}

	@AutoLogMethodInput(
		structuredMessage = true
	)
	public void inputStructuredMessageWithoutIgnoredParameters() {
		// Method for testing purpose only. Should not generate any warning.
	}

	@AutoLogMethodOutput(
		messageTemplate = "{}{}", voidOutputMessageTemplate = "{}", structuredMessage = true
	)
	public void outputWithIgnoredParameters() {
		// Method for testing purpose only.
	}

	@AutoLogMethodOutput(
		structuredMessage = true
	)
	public void outputStructuredMessageWithoutIgnoredParameters() {
		// Method for testing purpose only. Should not generate any warning.
	}
}