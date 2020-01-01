import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInput;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput;

@SuppressWarnings("AutoLogMethodAnnotationsConflict")
@AutoLogMethodInOut
@AutoLogMethodInput
@AutoLogMethodOutput
public class IgnoreWarningsWronglyAnnotatedTestClass {

	@AutoLogMethodInOut
	@AutoLogMethodInput
	@AutoLogMethodOutput
	public void badlyAnnotatedMethod() {
		// Method for testing purpose only.
	}

}