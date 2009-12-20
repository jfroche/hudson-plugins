package com.ikokoon.serenity.process;

import static org.junit.Assert.assertEquals;

import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.target.ITarget;
import com.ikokoon.target.Target;

/**
 * This is the test for the aggregator. The aggregator takes the collected data on the methods, classes and packages and calculates the metrics like
 * the abstractness the stability and so on.
 * 
 * @author Michael Couck
 * @since 02.08.09
 * @version 01.00
 */
public class AggregatorTest extends ATest implements IConstants {

	static {
		Configuration.getConfiguration().includedPackages.add(Target.class.getPackage().getName());
		Configuration.getConfiguration().includedPackages.add(ITarget.class.getPackage().getName());
		Configuration.getConfiguration().includedPackages.add("edu.umd.cs.findbugs");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void onePackageClassMethodAndLine() {
		DataBaseToolkit.clear(dataBase);
		Package pakkage = getPackage();
		Class klass = getClass(pakkage, Target.class.getName());
		Method method = getMethod(klass, "method name one", "method description one", 10, 10);
		Line line = getline(method, 1, 5);

		dataBase.persist(pakkage);

		Aggregator aggregator = new Aggregator(null, dataBase);
		aggregator.execute();

		assertEquals(10, pakkage.getLines());
		assertEquals(10, pakkage.getComplexity());
		assertEquals(10, pakkage.getCoverage());
		assertEquals(0, pakkage.getAbstractness());
		assertEquals(0.5, pakkage.getStability());
		assertEquals(0.582910995399281, pakkage.getDistance());
		assertEquals(0, pakkage.getInterfaces());
		assertEquals(1, pakkage.getImplementations());
		assertEquals(1, pakkage.getEfference());
		assertEquals(1, pakkage.getAfference());
		assertEquals(1, pakkage.getChildren().size());

		assertEquals(10, klass.getLines());
		assertEquals(10, klass.getComplexity());
		assertEquals(10, klass.getCoverage());
		assertEquals(0.5, klass.getStability());
		assertEquals(1, klass.getEfference());
		assertEquals(1, klass.getAfference());
		assertEquals(false, klass.getInterfaze());
		assertEquals(1, klass.getChildren().size());

		assertEquals(10, method.getComplexity());
		assertEquals(10, method.getLines());
		assertEquals(5, method.getExecuted());
		assertEquals(10, method.getCoverage());
		assertEquals(1, method.getChildren().size());

		assertEquals(1, line.getNumber());
		assertEquals(5, line.getCounter());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void onePackageThreeClassesTwoMethodsAndTwoLines() {
		DataBaseToolkit.clear(dataBase);
		Efferent efferent = new Efferent();
		efferent.setName(Logger.class.getPackage().getName());

		Package pakkage = getPackage();

		Class classTarget = getClass(pakkage, Target.class.getName());
		// classTarget.setInterfaze(true);
		Class classITarget = getClass(pakkage, ITarget.class.getName());
		classITarget.setInterfaze(true);

		classTarget.getEfferent().add(efferent);

		Method methodOne = getMethod(classTarget, "method name one", "method description one", 10, 10);
		getline(methodOne, 1, 5);
		getline(methodOne, 2, 10);

		Method methodTwo = getMethod(classTarget, "method name two", "method description two", 20, 5);
		getline(methodTwo, 1, 15);
		getline(methodTwo, 2, 20);

		Class classOne = getClass(pakkage, ITarget.class.getName());
		Method methodThree = getMethod(classOne, "method name three", "method description three", 15, 30);
		getline(methodThree, 1, 25);
		getline(methodThree, 2, 30);

		Method methodFour = getMethod(classOne, "method name four", "method description four", 40, 20);
		getline(methodFour, 1, 35);
		getline(methodFour, 2, 40);

		dataBase.persist(pakkage);

		Aggregator aggregator = new Aggregator(null, dataBase);
		aggregator.execute();

		assertEquals(65, pakkage.getLines());
		// Sigma : (class lines / package lines) * class complexity
		// ((15 / 65) * 13.333333333333332) + ((50 / 65) * 25) = 3.07692 + 19.2307 = 22.30692307692308
		assertEquals(22.30692307692308, pakkage.getComplexity());
		// ((15 / 65) * 26.666666666666664) + ((50 / 65) * 7.996) = 6.1538 + 6.5107 = 12.298461538461538
		assertEquals(12.298461538461538, pakkage.getCoverage());
		// i / (i + im) = 1 / 2 = 0.5
		assertEquals(0.5, pakkage.getAbstractness());
		// e / (e + a) = 3 / 5 = 0.6666666666666666
		// assertEquals(0.6666666666666666, pakkage.getStability());
		// d=|-stability + -abstractness + 1|/sqrt(-1²+-1²) = |-0.6666666666666666 + -0.5 + 1|sqrt(-1sq + -1sq) =
		// assertEquals(0.38860733026618743, pakkage.getDistance());
		assertEquals(1, pakkage.getInterfaces());
		assertEquals(2, pakkage.getImplementations());
		assertEquals(2, pakkage.getEfference());
		assertEquals(1, pakkage.getAfference());
		assertEquals(3, pakkage.getChildren().size());

		// Check the first class, the Target class
		assertEquals(15, classTarget.getLines()); // 10 + 15
		// Sigma n=1, n, (method lines / class lines) * method complexity
		// ((10 / 15) * 10) + ((5 / 15) * 20) = 6.666r + 6.6666r = 13.3333r
		assertEquals(13.333333333333332, classTarget.getComplexity());
		// ((10 / 15) * 20) + ((5 / 15) * 40) = 13.33r + 13.333r =
		assertEquals(26.666666666666664, classTarget.getCoverage());
		// e / e + a = 2 / 2 + 1 = 0.666r
		assertEquals(0.6666666666666666, classTarget.getStability());
		assertEquals(2, classTarget.getEfference());
		assertEquals(1, classTarget.getAfference());
		assertEquals(true, classITarget.getInterfaze());
		assertEquals(2, classTarget.getChildren().size());

		assertEquals(6.666666666666667, methodThree.getCoverage());
		assertEquals(55, methodThree.getExecuted());
		assertEquals(2, methodThree.getChildren().size());

		assertEquals(10, methodFour.getCoverage());
		assertEquals(75, methodFour.getExecuted());
		assertEquals(2, methodFour.getChildren().size());

		// Check the second class, the One class
		assertEquals(50, classOne.getLines()); // 30 + 20
		// Sigma n=1, n, (method lines / class lines) * method complexity
		// ((30 / 50) * 15) + ((20 / 50) * 40) = 9 + 16 = 25
		assertEquals(25, classOne.getComplexity());
		// ((30 / 50) * 6.666666666666667) + ((20 / 50) * 10.0) = 4.n2 + 4 = +-8
		assertEquals(7.996, classOne.getCoverage());
		// e / e + a = 1 / 1 + 1 = 0.5
		assertEquals(0.5, classOne.getStability());
		assertEquals(1, classOne.getEfference());
		assertEquals(1, classOne.getAfference());
		assertEquals(false, classOne.getInterfaze());
		assertEquals(2, classOne.getChildren().size());

		assertEquals(6.666666666666667, methodThree.getCoverage());
		assertEquals(55, methodThree.getExecuted());
		assertEquals(2, methodThree.getChildren().size());

		assertEquals(10, methodFour.getCoverage());
		assertEquals(75, methodFour.getExecuted());
		assertEquals(2, methodFour.getChildren().size());
	}

	@SuppressWarnings("unchecked")
	protected Package getPackage() {
		Package pakkage = new Package();
		pakkage.setName(Target.class.getPackage().getName());
		return pakkage;
	}

	@SuppressWarnings("unchecked")
	protected Class getClass(Package pakkage, String className) {
		Afferent afferent = new Afferent();
		afferent.setName(org.apache.log4j.Logger.class.getPackage().getName());
		Efferent efferent = new Efferent();
		efferent.setName(JUnitCore.class.getPackage().getName());

		Class klass = new Class();

		klass.setName(className);
		klass.getAfferent().add(afferent);
		klass.getEfferent().add(efferent);
		klass.setInterfaze(false);
		klass.setParent(pakkage);

		pakkage.getChildren().add(klass);

		return klass;
	}

	@SuppressWarnings("unchecked")
	protected Method getMethod(Class klass, String name, String description, double complexity, double lines) {
		Method method = new Method();
		method.setName(name);
		method.setClassName(klass.getName());
		method.setComplexity(complexity);
		method.setLines(lines);
		method.setDescription(description);
		method.setParent(klass);
		klass.getChildren().add(method);
		return method;
	}

	@SuppressWarnings("unchecked")
	protected Line getline(Method method, double number, double counter) {
		Line line = new Line();
		line.setClassName(method.getClassName());
		line.setMethodName(method.getName());
		line.setNumber(number);
		line.setCounter(counter);
		line.setParent(method);
		method.getChildren().add(line);
		return line;
	}

}