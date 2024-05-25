package pcd.ass03.ass01.simengine_conc.cli;

import pcd.ass03.ass01.simengine_conc.AbstractAgent;
import pcd.ass03.ass01.simengine_conc.AbstractEnvironment;
import pcd.ass03.ass01.simengine_conc.Flag;
import pcd.ass03.ass01.simengine_conc.SimulationListener;
import pcd.ass03.ass01.simtraffic_conc_examples.TrafficSimulationSingleRoadTwoCars;

import java.util.List;

/**
 * 
 * Main class to create and run a simulation - CLI
 * 
 */
public class RunTrafficSimulation {

	private static final int DEFAULT_STEPS = 10000;

	public static void main(String[] args) {
		var simulation = new TrafficSimulationSingleRoadTwoCars();
		// var simulation = new TrafficSimulationSingleRoadSeveralCars();
		// var simulation = new TrafficSimulationSingleRoadWithTrafficLightTwoCars();
		//var simulation = new TrafficSimulationWithCrossRoads();
		simulation.setup();
		var steps = 1000;

		RoadSimStatistics stat = new RoadSimStatistics();
		RoadSimView view = new RoadSimView();
		view.display();

		simulation.addSimulationListener(stat);
		simulation.addSimulationListener(new SimulationListener() {
			public void notifyInit(int t, List<AbstractAgent> agents, AbstractEnvironment env) {
				System.err.println("Init: " + t);
				view.notifyInit(t, agents, env);
			}

			public void notifyStepDone(int t, List<AbstractAgent> agents, AbstractEnvironment env) {
				System.err.println("Done: " + t);
				view.notifyStepDone(t, agents, env);
			}

		});

		Flag stopFlag = new Flag();
		simulation.run(steps, stopFlag, true);


	}
}
