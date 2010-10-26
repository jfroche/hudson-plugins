package hudson.plugins.violations.types.xmllint;


import org.junit.Assert;
import org.junit.Test;

public class XmllintParserTest {

	@Test
	public void testParseLineSimple() {
		XmllintParser parser = new XmllintParser();
        String err = "parts/omelette/affinitic/zamqp/contact-info.xml:17: parser error : Entity 'egrave' not defined\n";
		XmllintParser.XmllintViolation violation = parser.getXmllintViolation(err);
		Assert.assertEquals("The message is incorrect", "parser error : Entity 'egrave' not defined", violation.getMessage());
		Assert.assertEquals("The violation id is incorrect", "C", violation.getViolationId());
		Assert.assertEquals("The line str is incorrect", "17", violation.getLineStr());
		Assert.assertEquals("The file name is incorrect", "parts/omelette/affinitic/zamqp/contact-info.xml", violation.getFileName());
	}

	@Test
	public void testExtraViolationInfo() {
		XmllintParser parser = new XmllintParser();
		XmllintParser.XmllintViolation violation = parser.getXmllintViolation("parts/omelette/affinitic/zamqp/contact-info.xml:31: parser error : Premature end of data in tag html line 1");

		Assert.assertEquals("The message is incorrect", "parser error : Premature end of data in tag html line 1", violation.getMessage());
		Assert.assertEquals("The violation id is incorrect", "C", violation.getViolationId());
		Assert.assertEquals("The line str is incorrect", "31", violation.getLineStr());
		Assert.assertEquals("The file name is incorrect", "parts/omelette/affinitic/zamqp/contact-info.xml", violation.getFileName());
	}

}
