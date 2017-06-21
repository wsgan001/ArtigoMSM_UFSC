package br.ufsc.lehmann.msm.artigo.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;

public class LCSSClassifier implements IMeasureDistance<SemanticTrajectory> {

	private LCSS lcss;
	
	public LCSSClassifier(LCSSSemanticParameter... params) {
		lcss = new LCSS(params);
	}
	
	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return lcss.distance(t1, t2);
	}

	@Override
	public String name() {
		return "LCSS";
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		ArrayList<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		Random y = new Random(trajectories.size());
		for (SemanticTrajectory traj : trajectories) {
			entries.add(new DataEntry<SemanticTrajectory>(traj, y.nextBoolean() ? "chuva" : "sol"));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(trajectories.size(), 3),
				new LCSSClassifier(new LCSSSemanticParameter[] {//
						new LCSSSemanticParameter(Semantic.GEOGRAPHIC, 100.0), //
						new LCSSSemanticParameter(Semantic.TEMPORAL, 30 * 60 * 1000L), //
						new LCSSSemanticParameter(BikeDataReader.USER, null), //
						new LCSSSemanticParameter(BikeDataReader.GENDER, null), //
						new LCSSSemanticParameter(BikeDataReader.BIRTH_YEAR, null)
				}));
		Object classified = nn.classify(new DataEntry<SemanticTrajectory>(trajectories.get(0), "descubra"));
		System.out.println(classified);
	}
}