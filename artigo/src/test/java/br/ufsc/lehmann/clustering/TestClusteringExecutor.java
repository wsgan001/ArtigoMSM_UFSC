package br.ufsc.lehmann.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IClusteringExecutor;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.clusterers.ClusteringResult;
import smile.clustering.SpectralClustering;

public class TestClusteringExecutor implements IClusteringExecutor {
	
	private int classesCount;

	public TestClusteringExecutor(int classesCount) {
		this.classesCount = classesCount;
	}

	@Override
	public ClusteringResult cluster(Problem problem, IMeasureDistance<SemanticTrajectory> measureDistance) {
		List<SemanticTrajectory> training = new ArrayList<>(problem.data());
		double[][] distances = new double[training.size()][training.size()];
		for (int i = 0; i < training.size(); i++) {
			distances[i][i] = 0;
			final int finalI = i;
			IntStream.iterate(0, j -> j + 1).limit(i).parallel().forEach((j) -> {
				distances[finalI][j] = measureDistance.distance(training.get(finalI), training.get(j));
				distances[j][finalI] = distances[finalI][j];
			});
		}
		SpectralClustering clustering = new SpectralClustering(distances, classesCount);
		int[] clusterLabel = clustering.getClusterLabel();
		Multimap<Integer, SemanticTrajectory> clusteres = MultimapBuilder.hashKeys().arrayListValues().build();
		for (int i = 0; i < clusterLabel.length; i++) {
			clusteres.put(i, training.get(i));
		}
		return new ClusteringResult(clusteres.asMap().values(), clusterLabel);
	}

}
