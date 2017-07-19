package br.ufsc.lehmann.method;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMMoveClassifier;
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
			return new MSMMoveClassifier(new MSMSemanticParameter<Move, Number>(NElementProblem.move, null, 0.5),
					new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 0.0, 0.5),
					new MSMSemanticParameter<Number, Number>(NElementProblem.dataSemantic, null, 0.5));
		}
		if(problem instanceof NewYorkBusProblem) {
			return new MSMMoveClassifier(new MSMSemanticParameter(NewYorkBusDataReader.MOVE_SEMANTIC, null, 0.5),
					new MSMSemanticParameter(problem.semantics()[0], 50, 0.5),
					new MSMSemanticParameter(problem.semantics()[1], 100, 0.5));
		}
		if(problem instanceof DublinBusProblem) {
			return new MSMMoveClassifier(new MSMSemanticParameter(DublinBusDataReader.MOVE_SEMANTIC, null, 0.5),
					new MSMSemanticParameter(problem.semantics()[0], 50, 0.5),
					new MSMSemanticParameter(problem.semantics()[1], 100, 0.5));
		}
		if(problem instanceof NYBikeProblem) {
			return new MSMMoveClassifier(new MSMSemanticParameter(problem.semantics()[0], 50, 1/3),
					new MSMSemanticParameter(problem.semantics()[1], 0.5, 1/3),
					new MSMSemanticParameter(problem.semantics()[2], null, 1/3));
		}
		if(problem instanceof PatelProblem) {
			return new MSMMoveClassifier(new MSMSemanticParameter(PatelDataReader.MOVE_SEMANTIC, null, 0.5),
					new MSMSemanticParameter(Semantic.GEOGRAPHIC, 500, 0.5),
					new MSMSemanticParameter(Semantic.TEMPORAL, 100, 0.5));
		}
		return null;
	}
}