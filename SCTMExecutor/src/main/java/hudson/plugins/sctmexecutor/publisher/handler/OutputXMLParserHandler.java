package hudson.plugins.sctmexecutor.publisher.handler;

import hudson.plugins.sctmexecutor.publisher.SCTMTestCaseResult;
import hudson.plugins.sctmexecutor.publisher.SCTMTestResult;
import hudson.plugins.sctmexecutor.publisher.SCTMTestResult.TestState;
import hudson.plugins.sctmexecutor.publisher.SCTMTestSuiteResult;
import hudson.tasks.test.TestResult;

import java.util.Stack;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OutputXMLParserHandler extends DefaultHandler {
  private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmexecutor");

  private SCTMTestSuiteResult rootSuiteResult;
  private Stack<TestResult> resultStack;
  private Stack<String> cdataStack;
  private final String configuration;
  private boolean pushCData;
  private int incident;
  private int countMessages;
  private int countStacktraces;
  
  public OutputXMLParserHandler(SCTMTestSuiteResult suiteResult, String configuration) {
    this.rootSuiteResult = suiteResult;
    this.configuration = configuration;
    this.resultStack = new Stack<TestResult>();
    this.cdataStack = new Stack<String>();
    
    this.resultStack.push(rootSuiteResult);
  }
  
  @Override
  public void startDocument() throws SAXException {
    
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    pushCData = false;
    if ("TestSuite".equals(qName) || ("ResultElement".equals(qName))) {
      String name = attributes.getValue("TestItem");
      if (!name.equals(configuration)) {
        SCTMTestSuiteResult temp = (SCTMTestSuiteResult)this.resultStack.peek();
        SCTMTestSuiteResult child = temp.getChildSuiteByName(name);
        if (child == null) {
          child = new SCTMTestSuiteResult(name, temp.getOwner());
          temp.addChild(child);
          child.setParent(temp);
        }
        this.resultStack.push(child);
      }
    } else if ("Test".equals(qName)) {
      String name = attributes.getValue("TestItem");
      SCTMTestSuiteResult suite = (SCTMTestSuiteResult)this.resultStack.peek();
      SCTMTestCaseResult child = suite.getChildTestByName(name);
      if (child == null) {
        child = new SCTMTestCaseResult(name, suite.getOwner());
        suite.addChild(child);
        child.setParent(suite);
      }
      
      this.resultStack.push(child);
    } else if ("Timer".equals(qName) ||
        "WasSuccess".equals(qName))
      pushCData = true;
    else if ("Message".equals(qName)) {
      countMessages++;
      pushCData = true;
    } else if ("Info".equals(qName)) {
      countStacktraces++;
      pushCData = true;
    } else if ("Incident".equals(qName))
      incident++;
  }
  
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (pushCData) {
      String item = new String(ch, start, length);
      this.cdataStack.push(item);
    }
    pushCData = false;
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if ("TestSuite".equals(qName) || ("ResultElement".equals(qName))) {
      if (!resultStack.empty()) // in case of the configuration TestSuite the stack is empty
        this.resultStack.pop();
    } else if ("Test".equals(qName)) {
      SCTMTestCaseResult result = (SCTMTestCaseResult) this.resultStack.pop();
      StringBuilder errormsg = new StringBuilder();
      for (int i=0; i<incident; i++) {
        StringBuilder stacktrace = new StringBuilder(); 
        if (countStacktraces > 0) {
          stacktrace.append("<b>Stacktrace: </b><br/>");
          stacktrace.append(popStringFromCDataStack());
          countStacktraces--;
        }
        if (countMessages > 0) {
          errormsg.append("<b>Error: </b><br/>");
          errormsg.append(popStringFromCDataStack());
          countMessages--;
        }
        errormsg.append(stacktrace);
      }
      boolean success = popBooleanFromCDataStack();
      float duration = popFloatFromCDataStack();
      
      result.addConfigurationResult(this.configuration, 
          new SCTMTestResult(success ? TestState.PASSED: TestState.FAILED, duration, errormsg.toString()));
      incident = 0;
    } else if ("RunCount".equals(qName)) {
    } else if ("Timer".equals(qName)) {
    } else if ("WasSuccess".equals(qName)) {
    }
  }

  private String popStringFromCDataStack() {
    return this.cdataStack.pop();
  }

  private float popFloatFromCDataStack() {
    String string = this.cdataStack.pop();
    return Float.parseFloat(string);

  }

  private  boolean popBooleanFromCDataStack() {
    String string = this.cdataStack.pop();
    return Boolean.parseBoolean(string);
  }

}
