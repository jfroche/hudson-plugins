package hudson.plugins.violations.types.zptlint;


import org.junit.Assert;
import org.junit.Test;

public class ZptlintParserTest {

	@Test
	public void testParseLineSimple() {
		ZptlintParser parser = new ZptlintParser();
        String err = " Error in: parts/omelette/affinitic/zamqp/contact-info.cpt  zope.tal.taldefs.TALError: missing value for TAL attribute: 'define'  , at line 26, column 7  ";
		ZptlintParser.ZptlintViolation violation = parser.getZptlintViolation(err);
		Assert.assertEquals("The file name is incorrect", "parts/omelette/affinitic/zamqp/contact-info.cpt", violation.getFileName());
		Assert.assertEquals("The line str is incorrect", "26", violation.getLineStr());
		Assert.assertEquals("The message is incorrect", "zope.tal.taldefs.TALError: missing value for TAL attribute: 'define'", violation.getMessage());
		Assert.assertEquals("The violation id is incorrect", "C", violation.getViolationId());
	}

}
