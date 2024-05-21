package pcd.ass03.ass01.simengine_conc.gui;

import pcd.ass02.ass01.simtraffic_conc_examples.*;
import pcd.ass03.ass01.simtraffic_conc_examples.TrafficSimulationSingleRoadSeveralCars;

/**
 * 
 * Main class to create and run a simulation - with GUI
 * 
 */
public class RunTrafficSimulation {

	private static final int DEFAULT_STEPS = 10000;
	
	public static void main(String[] args) {		

		// var simulation = new TrafficSimulationSingleRoadTwoCars();
		 var simulation = new TrafficSimulationSingleRoadSeveralCars();
		// var simulation = new TrafficSimulationSingleRoadWithTrafficLightTwoCars();
		
		//var simulation = new TrafficSimulationWithCrossRoads();
		
        SimulationGUI gui = new SimulationGUI(DEFAULT_STEPS);

		SimulationController controller = new SimulationController(simulation);
		controller.attach(gui);
        gui.display();
				
	}
}
