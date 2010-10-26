package hudson.plugins.violations.types.pyflakes;


import org.junit.Assert;
import org.junit.Test;

public class PyflakesParserTest {

	@Test
	public void testParseLineSimple() {
		PyflakesParser parser = new PyflakesParser();
		PyflakesParser.PyflakesViolation violation = parser.getPyflakesViolation("trunk/src/python/cachedhttp.py:3: Line too long (85/80)");

		Assert.assertEquals("The message is incorrect", "Line too long (85/80)", violation.getMessage());
		Assert.assertEquals("The violation id is incorrect", "C", violation.getViolationId());
		Assert.assertEquals("The line str is incorrect", "3", violation.getLineStr());
		Assert.assertEquals("The file name is incorrect", "trunk/src/python/cachedhttp.py", violation.getFileName());
	}

	@Test
	public void testExtraViolationInfo() {
		PyflakesParser parser = new PyflakesParser();
		PyflakesParser.PyflakesViolation violation = parser.getPyflakesViolation("/ssd/svn/foo/bar/publisher.py:50: local variable 'backend' is assigned to but never used");

		Assert.assertEquals("The message is incorrect", "local variable 'backend' is assigned to but never used", violation.getMessage());
		Assert.assertEquals("The violation id is incorrect", "C", violation.getViolationId());
		Assert.assertEquals("The line str is incorrect", "50", violation.getLineStr());
		Assert.assertEquals("The file name is incorrect", "/ssd/svn/foo/bar/publisher.py", violation.getFileName());
	}

	@Test
	public void testExtraViolationInfo2() {
		PyflakesParser parser = new PyflakesParser();
		PyflakesParser.PyflakesViolation violation = parser.getPyflakesViolation("trunk/src/python/tv.py:35: Missing docstring");

		Assert.assertEquals("The message is incorrect", "Missing docstring", violation.getMessage());
		Assert.assertEquals("The violation id is incorrect", "C", violation.getViolationId());
		Assert.assertEquals("The line str is incorrect", "35", violation.getLineStr());
		Assert.assertEquals("The file name is incorrect", "trunk/src/python/tv.py", violation.getFileName());
	}
}
