package br.ufsc.lehmann.classifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.IClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.KNNSmileTrainer;
import br.ufsc.lehmann.msm.artigo.classifiers.KNNTrainer;
import br.ufsc.lehmann.msm.artigo.validation.Accuracy;
import br.ufsc.lehmann.msm.artigo.validation.ClassificationMeasure;
import br.ufsc.lehmann.msm.artigo.validation.FDR;
import br.ufsc.lehmann.msm.artigo.validation.FMeasure;
import br.ufsc.lehmann.msm.artigo.validation.Fallout;
import br.ufsc.lehmann.msm.artigo.validation.Precision;
import br.ufsc.lehmann.msm.artigo.validation.Recall;
import br.ufsc.lehmann.msm.artigo.validation.Specificity;
import br.ufsc.lehmann.msm.artigo.validation.Validation;

@RunWith(Parameterized.class)
public abstract class AbstractClassifierTest {
	
    private static final FDR FDR = new FDR();
	private static final Fallout FALLOUT = new Fallout();
	private static final Specificity SPECIFICITY = new Specificity();
	private static final FMeasure F_MEASURE = new FMeasure();
	private static final Recall RECALL = new Recall();
	private static final Precision PRECISION = new Precision();
	private static final Accuracy ACCURACY = new Accuracy();

	@Rule public TestName name = new TestName();
    
    @Parameters(name="{0}")
    public static Collection<EnumProblem> data() {
        return Arrays.asList(EnumProblem.values());
    }
	private Problem problem;
	private Multimap<ClassificationMeasure, String> measureFailures = MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
	
	public AbstractClassifierTest(EnumProblem problemDescriptor) {
		problem = problemDescriptor.problem();
	}
	
	@Before
	public void before() {
		System.out.println(getClass().getSimpleName() + "#" + name.getMethodName());
	}
	
	@After
	public void after() {
		if(!measureFailures.isEmpty()) {
			StringWriter sw = new StringWriter();
			for (Map.Entry<ClassificationMeasure, Collection<String>> entry : measureFailures.asMap().entrySet()) {
				sw.append(entry.getKey().toString()).append(" - [");
				Collection<String> value = entry.getValue();
				for (Iterator iterator = value.iterator(); iterator.hasNext();) {
					String message = (String) iterator.next();
					sw.append(message);
					if(iterator.hasNext()) {
						sw.append(", ");
					}
				}
				sw.append("]\n");
			}
			Assert.fail(sw.toString());
		}
	}

	@Test
	public void simpleClassification() throws Exception {
		HashMap<Object, DescriptiveStatistics> stats = new HashMap<>();
		for (int i = 0; i < 5; i++) {
			stats.put(String.valueOf(i), new DescriptiveStatistics());
		}
		TestClassificationExecutor executor = new TestClassificationExecutor(stats);
		NElementProblem problem = new NElementProblem(15, 5);
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		try {
			executor.classifyProblem(problem, classifier);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 5; i++) {
			assertEquals(1.0, stats.get(String.valueOf(i)).getMean(), 0.000001);
			assertEquals(2, stats.get(String.valueOf(i)).getValues().length);
		}
	}
	
	@Test
	public void validation_accuracy() throws Exception {
//		NElementProblem problem = new NElementProblem(150, 10);
		List<SemanticTrajectory> data = problem.data();
		List<SemanticTrajectory> testingData = problem.testingData();
		List<SemanticTrajectory> trainingData = new ArrayList<>(problem.trainingData());
		trainingData.addAll(testingData);
		SemanticTrajectory[] trainData = trainingData.toArray(new SemanticTrajectory[trainingData.size()]);
		List<SemanticTrajectory> validatingData = new ArrayList<>(problem.validatingData());
		SemanticTrajectory[] validateData = validatingData.toArray(new SemanticTrajectory[validatingData.size()]);
		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
		Object[] validateLabelData = new Object[validateData.length];
		Object[] allLabelData = new Object[allData.length];
		Semantic discriminator = problem.discriminator();
		for (int i = 0; i < allData.length; i++) {
			allLabelData[i] = discriminator.getData(allData[i], 0);
		}
		for (int i = 0; i < validateData.length; i++) {
			validateLabelData[i] = discriminator.getData(validateData[i], 0);
		}
		SemanticTrajectory[] testData = testingData.toArray(new SemanticTrajectory[testingData.size()]);
		Object[] testLabelData = new Object[testData.length];
		for (int i = 0; i < testLabelData.length; i++) {
			testLabelData[i] = discriminator.getData(testData[i], 0);
		}
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		Validation validation = new Validation(problem, classifier);

		KNNSmileTrainer<Object> trainer = new KNNSmileTrainer<>();
//		IClassifier<Object> train = trainer.train(trainData, discriminator, classifier);
//		double testingAccuracy = validation.<Object> test(train, testData, testLabelData, ACCURACY);
//		System.out.println(testingAccuracy);
//		assertTrue("Testing accuracy = " + testingAccuracy, testingAccuracy > .8);
//		
		double validationAccuracy = validation.<Object> cv(10, trainer, allData, allLabelData, ACCURACY);
		System.out.println(validationAccuracy);
		assertMeasure(ACCURACY, "Validating accuracy = " + validationAccuracy, validationAccuracy > .8);
	}
	
	@Test
	public void validation_precision_recall() throws Exception {
//		NElementProblem problem = new NElementProblem(150, 10);
		List<SemanticTrajectory> testingData = problem.testingData();
		List<SemanticTrajectory> trainingData = new ArrayList<>(problem.trainingData());
		trainingData.addAll(testingData);
		SemanticTrajectory[] trainData = trainingData.toArray(new SemanticTrajectory[trainingData.size()]);
		List<SemanticTrajectory> validatingData = new ArrayList<>(problem.validatingData());
		SemanticTrajectory[] validateData = validatingData.toArray(new SemanticTrajectory[validatingData.size()]);
		Object[] validateLabelData = new Object[validateData.length];
		Semantic discriminator = problem.discriminator();
		for (int i = 0; i < validateData.length; i++) {
			validateLabelData[i] = discriminator.getData(validateData[i], 0);
		}
		SemanticTrajectory[] testData = testingData.toArray(new SemanticTrajectory[testingData.size()]);
		Object[] testLabelData = new Object[testData.length];
		for (int i = 0; i < testLabelData.length; i++) {
			testLabelData[i] = discriminator.getData(testData[i], 0);
		}
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		Validation validation = new Validation(problem, classifier);
		ClassificationMeasure[] measures = new ClassificationMeasure[] {//
				PRECISION,//
				RECALL,//
				F_MEASURE,//
				SPECIFICITY,//
				FALLOUT,//
				FDR
				};

		KNNSmileTrainer<Object> trainer = new KNNSmileTrainer<>();
		IClassifier<Object> train = trainer.train(trainData, discriminator, classifier);
		double[] testingMensures = validation.<Object> test(train, testData, testLabelData, new Binarizer(testLabelData[0]), measures);
		System.out.println(Arrays.toString(testingMensures));
		assertMeasure(PRECISION, "Testing precision = " + testingMensures[0], testingMensures[0] > .8);
		
		double[] validationAccuracy = validation.<Object> test(train, validateData, validateLabelData, new Binarizer(validateLabelData[0]), measures);
		System.out.println(Arrays.toString(validationAccuracy));
		assertMeasure(PRECISION, "Validation precision = " + validationAccuracy[0], validationAccuracy[0] > .8);
		assertMeasure(RECALL, "Recall = " + validationAccuracy[1], validationAccuracy[1] > .8);
		assertMeasure(F_MEASURE, "F-Measure = " + validationAccuracy[2], validationAccuracy[2] > .8);
		assertMeasure(SPECIFICITY, "Specificity = " + validationAccuracy[3], validationAccuracy[3] > .8);
		assertMeasure(FALLOUT, "False alarm rate = " + validationAccuracy[4], validationAccuracy[4] < .2);
		assertMeasure(FDR, "False discovery rate = " + validationAccuracy[5], validationAccuracy[5] < .2);
	}
	
	@Test
	@Ignore
	public void crossValidation_10_accuracy() throws Exception {
		HashMap<Object, DescriptiveStatistics> stats = new HashMap<>();
		for (int i = 0; i < 15; i++) {
			stats.put(String.valueOf(i), new DescriptiveStatistics());
		}
		NElementProblem problem = new NElementProblem(150, 10);
		SemanticTrajectory[] data = problem.data().toArray(new SemanticTrajectory[problem.data().size()]);
		Object[] dataLabel = new Object[data.length];
		for (int i = 0; i < data.length; i++) {
			dataLabel[i] = problem.discriminator().getData(data[i], 0);
		}
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		Validation validation = new Validation(problem, classifier);
		double accuracy = validation.<SemanticTrajectory> cv(10, new KNNTrainer<>(), data, dataLabel, ACCURACY);
		System.out.println(accuracy);
		assertMeasure(ACCURACY, "Accuracy = " + accuracy, accuracy > .8);
	}
	
	@Test
	@Ignore
	public void crossValidation_10_precision_recall() throws Exception {
		HashMap<Object, DescriptiveStatistics> stats = new HashMap<>();
		for (int i = 0; i < 15; i++) {
			stats.put(String.valueOf(i), new DescriptiveStatistics());
		}
//		NElementProblem problem = new NElementProblem(150, 10);
		SemanticTrajectory[] data = problem.data().toArray(new SemanticTrajectory[problem.data().size()]);
		Object[] dataLabel = new Object[data.length];
		for (int i = 0; i < data.length; i++) {
			dataLabel[i] = problem.discriminator().getData(data[i], 0);
		}
		IMeasureDistance<SemanticTrajectory> classifier = measurer(problem);
		Validation validation = new Validation(problem, classifier);
		ClassificationMeasure[] measures = new ClassificationMeasure[] {//
				PRECISION,//
				RECALL,//
				F_MEASURE,//
				SPECIFICITY,//
				FALLOUT,//
				FDR
				};
		double[] precision = validation.<SemanticTrajectory> cv(10, new KNNTrainer<>(), data, dataLabel, new Binarizer(dataLabel[0]), measures);
		System.out.println(Arrays.toString(precision));
		assertMeasure(PRECISION, "Precision = " + precision[0], precision[0] > .8);
		assertMeasure(RECALL, "Recall = " + precision[1], precision[1] > .8);
		assertMeasure(F_MEASURE, "F-Measure = " + precision[2], precision[2] > .8);
		assertMeasure(SPECIFICITY, "Specificity = " + precision[3], precision[3] > .8);
		assertMeasure(FALLOUT, "False alarm rate = " + precision[4], precision[4] < .2);
		assertMeasure(FDR, "False discovery rate = " + precision[5], precision[5] < .2);
	}
	
	public void assertMeasure(ClassificationMeasure measure, double expected, double actual, double delta) {
		if(expected != actual) {
			measureFailures.put(measure, "Expected was " + expected + " but actual is " + actual);
		}
	}
	
	public void assertMeasure(ClassificationMeasure measure, String message, double expected, double actual, double delta) {
		if(expected != actual) {
			measureFailures.put(measure, message);
		}
	}
	
	public void assertMeasure(ClassificationMeasure measure, boolean test) {
		if(!test) {
			measureFailures.put(measure, "Expected true but actual is false");
		}
	}
	
	public void assertMeasure(ClassificationMeasure measure, String message, boolean test) {
		if(!test) {
			measureFailures.put(measure, message);
		}
	}
	
	abstract IMeasureDistance<SemanticTrajectory> measurer(Problem problem);
}
