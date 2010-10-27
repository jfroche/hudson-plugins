package hudson.plugins.violations.types.pep8;


import org.junit.Assert;
import org.junit.Test;

public class Pep8ParserTest {

	@Test
	public void testParseLineSimple() {
		Pep8Parser parser = new Pep8Parser();
		Pep8Parser.Pep8Violation violation = parser.getPep8Violation("parts/omelette/affinitic/zamqp/message.py:28:80: E501 line too long (95 characters)");

		Assert.assertEquals("The message is incorrect", "E501 line too long (95 characters)", violation.getMessage());
		Assert.assertEquals("The violation id is incorrect", "C", violation.getViolationId());
		Assert.assertEquals("The line str is incorrect", "28", violation.getLineStr());
		Assert.assertEquals("The file name is incorrect", "parts/omelette/affinitic/zamqp/message.py", violation.getFileName());
	}
}
