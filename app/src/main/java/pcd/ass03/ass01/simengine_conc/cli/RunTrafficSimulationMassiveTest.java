package pcd.ass03.ass01.simengine_conc.cli;

import pcd.ass03.ass01.simtraffic_conc_examples.TrafficSimulationSingleRoadMassiveNumberOfCars;

public class RunTrafficSimulationMassiveTest {

	public static void main(String[] args) {		

		int numCars = 5000;
		int nSteps = 100;
		
		var simulation = new TrafficSimulationSingleRoadMassiveNumberOfCars(numCars);
		int nWorkers = Runtime.getRuntime().availableProcessors() + 1;
		simulation.configureNumWorkers(nWorkers);
		simulation.setup();
		
		/*
		RoadMassiveSimView view = new RoadMassiveSimView();
		view.display();
		simulation.addSimulationListener(view);		
		*/
		
		log("Running the simulation: " + numCars + " cars, for " + nSteps + " steps ...");
		
		simulation.run(nSteps, false);

		long d = simulation.getSimulationDuration();
		log("Completed in " + d + " ms - average time per step: " + simulation.getAverageTimePerStep() + " ms");
	}
	
	private static void log(String msg) {
		System.out.println("[ SIMULATION ] " + msg);
	}
}
