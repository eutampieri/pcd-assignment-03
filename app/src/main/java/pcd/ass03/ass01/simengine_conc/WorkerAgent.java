package pcd.ass03.ass01.simengine_conc;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class WorkerAgent extends AbstractBehavior<Message> {
    private final AbstractAgent assignedSimAgent;
    private boolean simulationIsRunning = true;

    public WorkerAgent(ActorContext<Message> context, AbstractAgent assignedSimAgents) {
        super(context);
        this.assignedSimAgent = assignedSimAgents;
    }

    public static Behavior<Message> create(AbstractAgent assignedSimAgents) {
        return Behaviors.setup(context -> new WorkerAgent(context, assignedSimAgents));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Message.WorkerCommand.class, this::onCommand)
                .onMessage(Message.Stop.class, (r) -> {
                    this.simulationIsRunning = false;
                    return Behaviors.same();
                })
                .build();
    }

    private Behavior<Message> onCommand(Message.WorkerCommand command) {
        if (this.simulationIsRunning) {
            assignedSimAgent.step(((Message.WorkerCommand)command).dt);
            ((Message.WorkerCommand)command).replyTo.tell(new Message.Response());
        }
        return Behaviors.same();
    }
}
