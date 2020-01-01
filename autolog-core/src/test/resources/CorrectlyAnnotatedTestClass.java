import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInput;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput;

public class CorrectlyAnnotatedTestClass {

	@AutoLogMethodInOut
	public void correctlyAnnotatedMethod() {
		// Method for testing purpose only.
	}

	@AutoLogMethodInput
	@AutoLogMethodOutput
	public void anotherCorrectlyAnnotatedMethod() {
		// Method for testing purpose only.
	}

}