package pcd.ass03.ass01.simengine_conc.gui;

import pcd.ass03.ass01.simengine_conc.AbstractSimulation;

public class SimulationController {

	private AbstractSimulation simulation;
	private SimulationGUI gui;
	private RoadSimView view;
	private RoadSimStatistics stat;
	 
	public SimulationController(AbstractSimulation simulation) {
		this.simulation = simulation;
	}
	
	public void attach(SimulationGUI gui) {
		this.gui = gui;		
		view = new RoadSimView();
		stat = new RoadSimStatistics();
		simulation.addSimulationListener(stat);
		simulation.addSimulationListener(view);		
		gui.setController(this);
	}

	public void notifyStarted(int nSteps) {
		// uso thread separato per renderlo asincrono rispetto alla gui
		new Thread(() -> {
			simulation.setup();			
			view.display();
		
			simulation.run(nSteps, true);
			gui.reset();
			
		}).start();
	}
	
	public void notifyStopped() {
		this.simulation.requestStop();
	}

}
