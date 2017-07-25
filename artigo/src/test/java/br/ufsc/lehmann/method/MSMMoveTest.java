package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.MSM_Move.MSMMoveSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMMoveClassifier;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.NYBikeProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;

public interface MSMMoveTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(NElementProblem.stop, null, NElementProblem.move, null, 0.5),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 0.0, 0.5),
					new MSMSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null, 0.5));
		}
		if(problem instanceof NewYorkBusProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(NewYorkBusDataReader.STOP_SEMANTIC, 100, NewYorkBusDataReader.MOVE_SEMANTIC, 10, 0.5),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, 50, 0.5),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, 100, 0.5));
		}
		if(problem instanceof DublinBusProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(DublinBusDataReader.STOP_SEMANTIC, 100, DublinBusDataReader.MOVE_SEMANTIC, 10, 0.5),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_LATLON, 50, 0.5),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, 100, 0.5));
		}
		if(problem instanceof NYBikeProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(problem.semantics()[0], 50, problem.semantics()[0], 50, 1/3),
					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, 0.5, 1/3),
					new MSMSemanticParameter<String, Number>(BikeDataReader.USER, null, 1/3));
		}
		if(problem instanceof PatelProblem) {
			return new MSMMoveClassifier(new MSMMoveSemanticParameter(PatelDataReader.STOP_SEMANTIC, 500, PatelDataReader.MOVE_SEMANTIC, 10, 1));
//					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC_EUCLIDEAN, 500, 0.5),
//					new MSMSemanticParameter<TemporalDuration, Number>(Semantic.TEMPORAL, 100, 0.5));
		}
		return null;
	}
}
