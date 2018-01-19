package br.ufsc.lehmann.similarity;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.SMSMTemporalDurationTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class H_MSM_StopMove_TemporalDurationCompactnessTest extends AbstractCompactnessTest implements SMSMTemporalDurationTest {

	public H_MSM_StopMove_TemporalDurationCompactnessTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return SMSMTemporalDurationTest.super.measurer(problem);
	}

}