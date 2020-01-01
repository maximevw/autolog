import com.github.maximevw.autolog.core.annotations.AutoLogPerformance;

@AutoLogPerformance(additionalDataProvider = "anotherWrongAdditionalDataProvider")
public class WrongAdditionalDataProviderTestClass {

	private Object wrongAdditionalDataProvider = new Object();
	private Object anotherWrongAdditionalDataProvider = new Object();

	@AutoLogPerformance(additionalDataProvider = "wrongAdditionalDataProvider")
	public void methodWithWrongAdditionalDataProvider() {
		// Method for testing purpose only.
	}
}