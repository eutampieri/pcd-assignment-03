package pcd.ass03.ass01.simengine_conc;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Receive;


import java.util.ArrayList;
import java.util.List;

public class MasterAgent extends AbstractBehavior<Message> {
    private final AbstractSimulation sim;
    private final int numSteps;
    private final List<ActorRef<Message>> workers;
    private int pendingResponses;
    private boolean toBeInSyncWithWallTime;
    private int nStepsPerSec;
    private long currentWallTime;
    private int completedSteps = 0;
    private int t = 0;
    private boolean simulationIsRunning = true;

    public MasterAgent(ActorContext<Message> context, AbstractSimulation sim, int nWorkers, int numSteps, boolean syncWithTime) {
        super(context);
        this.sim = sim;
        this.numSteps = numSteps;
        this.workers = new ArrayList<>();
        this.pendingResponses = sim.getAgents().size();
        if (syncWithTime) {
            this.syncWithTime(25);
        }
        this.currentWallTime = System.currentTimeMillis();
        createActors();
    }

    public static Behavior<Message> create(AbstractSimulation sim, int nWorkers, int numSteps, boolean syncWithTime) {
        return Behaviors.setup(context -> new MasterAgent(context, sim, nWorkers, numSteps, syncWithTime));
    }

    private void createActors() {
        var simAgents = sim.getAgents();
        for (int i = 0; i < simAgents.size(); i++) {
            AbstractAgent agent = simAgents.get(i);
            workers.add(getContext().spawn(WorkerAgent.create(agent), "worker-" + i));
        }
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Message.Command.class, this::onCommand)
                .onMessage(Message.Response.class, this::onResponse)
                .onMessage(Message.Stop.class, (r) -> {
                    this.simulationIsRunning = false;
                    this.workers.forEach((x) -> x.tell(r));
                    return Behaviors.same();
                })
                .build();
    }

    private Behavior<Message> onCommand(Message.Command command) {
        startSimulation(command.numSteps);
        return Behaviors.same();
    }

    private Behavior<Message> onResponse(Message.Response response) {
        pendingResponses--;
        if (pendingResponses == 0) {
            stepSimulation();
        }
        return Behaviors.same();
    }

    private void startSimulation(int numSteps) {
        var simEnv = sim.getEnvironment();
        var simAgents = sim.getAgents();
        this.simulationIsRunning = true;

        simEnv.init();
        for (var a : simAgents) {
            a.init(simEnv);
        }

        int t = sim.getInitialTime();

        sim.notifyReset(t, simAgents, simEnv);
        stepSimulation(true);
    }

    private void stepSimulation() {
        stepSimulation(false);
    }

    private void stepSimulation(boolean firstExecution) {
        if (firstExecution || (simulationIsRunning && pendingResponses == 0)) {
            var simEnv = sim.getEnvironment();
            var simAgents = sim.getAgents();
            int dt = sim.getTimeStep();

            simEnv.step(dt);
            simEnv.processActions();

            System.err.println("t = " + t + ", dt = " + dt);
            sim.notifyNewStep(t, simAgents, simEnv);
            System.out.println(t + " " + simAgents + " " + simEnv);

            try {
                if (toBeInSyncWithWallTime) {
                    syncWithWallTime();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            // Incrementa il numero di step completati
            completedSteps++;
            // Se abbiamo ancora step da eseguire, avvia un nuovo ciclo di simulazione
            if (completedSteps < numSteps) {
                this.pendingResponses = sim.getAgents().size();
                // Invia un nuovo comando ai workers per eseguire un altro step
                sim.getEnvironment().step(dt);
                for (ActorRef<Message> worker : workers) {
                    worker.tell(new Message.WorkerCommand(dt, getContext().getSelf()));
                }
                t += dt;

            } else {
                // Se abbiamo completato tutti gli step, ferma la simulazione
                getContext().getLog().info("Simulation completed.");
                this.simulationIsRunning = false;
            }
            simEnv.cleanActions();
        }
        if(!simulationIsRunning) {
            this.getContext().getSystem().terminate();
        }
    }

    private void syncWithTime(int nStepsPerSec) {
        this.toBeInSyncWithWallTime = true;
        this.nStepsPerSec = nStepsPerSec;
    }

    private void syncWithWallTime() throws InterruptedException {
        long newWallTime = System.currentTimeMillis();
        long delay = 1000 / this.nStepsPerSec;
        long wallTimeDT = newWallTime - currentWallTime;
        currentWallTime = System.currentTimeMillis();
        if (wallTimeDT < delay) {
            Thread.sleep(delay - wallTimeDT);
        }
    }

}
