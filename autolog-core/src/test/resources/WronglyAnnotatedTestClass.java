import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInput;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput;

@AutoLogMethodInOut
@AutoLogMethodInput
@AutoLogMethodOutput
public class WronglyAnnotatedTestClass {

	@SuppressWarnings("fakeSuppressWarnings")
	@AutoLogMethodInOut
	@AutoLogMethodInput
	@AutoLogMethodOutput
	public void badlyAnnotatedMethod() {
		// Method for testing purpose only.
	}

	@SuppressWarnings("AutoLogMethodAnnotationsConflict")
	@AutoLogMethodInOut
	@AutoLogMethodInput
	@AutoLogMethodOutput
	public void methodWithSuppressWarnings() {
		// Method for testing purpose only.
	}

}