package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.Problem;
import smile.math.Random;

public class DublinBusProblem implements Problem {
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;
	private boolean loaded;
	private String[] lines;
	private Random random = new Random();

	public DublinBusProblem(String... lines) {
		this.lines = lines;
	}
	
	@Override
	public Problem clone(Random r) {
		DublinBusProblem ret = new DublinBusProblem(lines);
		ret.random = r;
		return ret;
	}

	@Override
	public Semantic[] semantics() {
		return new Semantic[] {
			 Semantic.GEOGRAPHIC_LATLON, //
			 Semantic.TEMPORAL,//
			// DublinBusDataReader.OPERATOR,
			DublinBusDataReader.STOP,
			DublinBusDataReader.STOP_MOVE_SEMANTIC
		};
	}

	@Override
	public List<SemanticTrajectory> data() {
		if(!loaded) {
			load();
		}
		return data;
	}

	@Override
	public Semantic discriminator() {
		return DublinBusDataReader.LINE_INFO;
	}

	@Override
	public List<SemanticTrajectory> trainingData() {
		if(!loaded) {
			load();
		}
		return trainingData;
	}

	@Override
	public List<SemanticTrajectory> testingData() {
		if(!loaded) {
			load();
		}
		return testingData;
	}

	@Override
	public List<SemanticTrajectory> validatingData() {
		if(!loaded) {
			load();
		}
		return validatingData;
	}

	@Override
	public String shortDescripton() {
		return "Dublin bus" + (lines != null ? "(lines=" + lines.length + ")" : "");
	}

	private void load() {
		if(loaded) {
			return;
		}
		try {
			data = new ArrayList<>(new DublinBusDataReader().read(lines));
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
		Collections.shuffle(data, new java.util.Random() {
			@Override
			public int nextInt(int bound) {
				return random.nextInt(bound);
			}
			
			@Override
			public int nextInt() {
				return random.nextInt();
			}
		});
		this.trainingData = data.subList(0, (int) (data.size() * (1.0 / 3)));
		this.testingData = data.subList((int) (data.size() * (1.0 / 3) + 1), (int) (data.size() * (2.0 / 3)));
		this.validatingData = data.subList((int) (data.size() * (2.0 / 3) + 1), data.size() - 1);
		this.loaded = true;
	}

}
