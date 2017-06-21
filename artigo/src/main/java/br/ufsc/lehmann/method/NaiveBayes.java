package br.ufsc.lehmann.method;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.BikeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.BirthYearSemantic;
import br.ufsc.lehmann.msm.artigo.problems.Climate;
import br.ufsc.lehmann.msm.artigo.problems.ClimateTemperatureSemantic;
import br.ufsc.lehmann.msm.artigo.problems.ClimateWeatherSemantic;
import br.ufsc.lehmann.msm.artigo.problems.ClimateWindSpeedSemantic;
import br.ufsc.lehmann.msm.artigo.problems.Gender;
import br.ufsc.lehmann.msm.artigo.problems.UserType;
import smile.classification.NaiveBayes.Model;
import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;

public class NaiveBayes {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		BasicSemantic<String> userSemantic = new BasicSemantic<>(2);
		BasicSemantic<String> genderSemantic = new BasicSemantic<>(3);
		BirthYearSemantic birthSemantic = new BirthYearSemantic(4);
		ClimateTemperatureSemantic tempSemantic = new ClimateTemperatureSemantic(5, .5);
		ClimateWindSpeedSemantic windSemantic = new ClimateWindSpeedSemantic(6, .1);
		ClimateWeatherSemantic weatherSemantic = new ClimateWeatherSemantic(7);
		List<SemanticTrajectory> trajectories = new BikeDataReader().read();
		smile.classification.NaiveBayes bayes = new smile.classification.NaiveBayes(Model.MULTINOMIAL, 3, 5);
		NaiveBayesMultinomialUpdateable naive = new NaiveBayesMultinomialUpdateable();
		for (SemanticTrajectory traj : trajectories) {
			for (int i = 0; i < traj.length() - 1; i++) {
				List<Climate> weathers = weatherSemantic.getData(traj, i);
				for (int j = 0; j < weathers.size(); j++) {
					String birthYear = birthSemantic.getData(traj, i);
					bayes.learn(new double[] {
							UserType.valueOf(userSemantic.getData(traj, i)).getId(),
							Integer.parseInt(birthYear.equals("\\N") ? "0" : birthYear),
							(tempSemantic.getData(traj, i)),
							(windSemantic.getData(traj, i)),
							weathers.get(j).ordinal()
					}, Gender.fromId(genderSemantic.getData(traj, i)).getId());
				}
			}
		}
		System.out.println(Arrays.toString(bayes.getPriori()));
		
	}

}