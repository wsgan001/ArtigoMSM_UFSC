package br.ufsc.lehmann.msm.artigo.clusterers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableLong;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.lehmann.msm.artigo.IClusteringExecutor;
import br.ufsc.lehmann.msm.artigo.Trajectories;
import br.ufsc.lehmann.msm.artigo.classifiers.LCSSClassifier;
import br.ufsc.lehmann.msm.artigo.clusterers.dissimilarity.CompleteLinkDissimilarity;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.AdjustedRandIndex;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.DunnIndex;
import br.ufsc.lehmann.msm.artigo.clusterers.evaluation.intra.MaxDistance;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import smile.clustering.SpectralClustering;

public class SpectralCluster implements IClusteringExecutor {
	
	private int numOfClasses;

	public SpectralCluster(int numOfClasses) {
		this.numOfClasses = numOfClasses;
	}

	@Override
	public <E, T> ClusteringResult cluster(List<SemanticTrajectory> data, IMeasureDistance<SemanticTrajectory> measureDistance, Semantic<E, T> discriminator) {
		List<SemanticTrajectory> training = new ArrayList<>(data);
		double[][] distances = new double[training.size()][training.size()];
		for (int i = 0; i < training.size(); i++) {
			distances[i][i] = 0;
			final int finalI = i;
			IntStream.iterate(0, j -> j + 1).limit(training.size()).parallel().forEach((j) -> {
				if(finalI < j) {
					distances[finalI][j] = measureDistance.distance(training.get(finalI), training.get(j));
					distances[j][finalI] = distances[finalI][j];
				}
			});
		}
		SpectralClustering clustering = new SpectralClustering(distances, numOfClasses);
		int[] clusterLabel = clustering.getClusterLabel();
		Multimap<Integer, SemanticTrajectory> clusteres = MultimapBuilder.hashKeys().arrayListValues().build();
		for (int i = 0; i < clusterLabel.length; i++) {
			clusteres.put(i, training.get(i));
		}
		return new ClusteringResult(data, (List) clusteres.asMap().values(), clusterLabel, new Comparator<SemanticTrajectory>() {

			@Override
			public int compare(SemanticTrajectory o1, SemanticTrajectory o2) {
				return discriminator.similarity(discriminator.getData(o1, 0), discriminator.getData(o2, 0)) == 1.0 ? 0 : 1;
			}
		});
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		NewYorkBusProblem problem = new NewYorkBusProblem();
		List<SemanticTrajectory> data = problem.data();
		LCSSClassifier lcssClassifier = new LCSSClassifier(new LCSSSemanticParameter(Semantic.SPATIAL_LATLON, 50),
				new LCSSSemanticParameter(NewYorkBusDataReader.STOP_CENTROID_SEMANTIC, 100));
		LCSS lcss = new LCSS(new LCSSSemanticParameter(Semantic.SPATIAL_LATLON, 50),
				new LCSSSemanticParameter(NewYorkBusDataReader.STOP_CENTROID_SEMANTIC, 100));
		SpectralCluster spectralCluster = new SpectralCluster(2);
		ClusteringResult clusteringResult = spectralCluster.cluster(problem.data(), lcssClassifier, NewYorkBusDataReader.ROUTE);
		List<List<SemanticTrajectory>> clusteres = clusteringResult.getClusteres();
		Map<String, MutableLong> counters = new HashMap<>();
		for (int i = 0; i < clusteres.size(); i++) {
			for (SemanticTrajectory trajs : clusteres.get(i)) {
				String route = NewYorkBusDataReader.ROUTE.getData(trajs, 0);
				MutableLong counter = counters.get(route + "_" + i);
				if (counter == null) {
					counter = new MutableLong(0L);
					counters.put(route + "_" + i, counter);
				}
				counter.increment();
			}
		}
		for (Map.Entry<String, MutableLong> entry : counters.entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue().getValue());
		}
		AdjustedRandIndex<String> randIndex = new AdjustedRandIndex<String>();
		double value = randIndex.evaluate(clusteringResult.getClusterLabel(), new Trajectories<>(data, problem.discriminator()));
		System.out.println("Adjusted Rand-index: " + value);
		DunnIndex<String> dunnIndex = new DunnIndex<>(new MaxDistance(lcssClassifier), new CompleteLinkDissimilarity(lcssClassifier));
		value = dunnIndex.evaluate(clusteringResult.getClusterLabel(), new Trajectories<>(data, problem.discriminator()));
		System.out.println("Dunn index: " + value);
	}
}
