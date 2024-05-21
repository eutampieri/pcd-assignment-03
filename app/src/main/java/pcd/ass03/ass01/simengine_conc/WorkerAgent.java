package pcd.ass03.ass01.simengine_conc;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class WorkerAgent extends AbstractBehavior<Message> {



    private final AbstractAgent assignedSimAgent;
    private final Flag stopFlag;

    public WorkerAgent(ActorContext<Message> context, AbstractAgent assignedSimAgents, Flag stopFlag) {
        super(context);
        this.assignedSimAgent = assignedSimAgents;
        this.stopFlag = stopFlag;
    }

    public static Behavior<Message> create(AbstractAgent assignedSimAgents, Flag stopFlag) {
        return Behaviors.setup(context -> new WorkerAgent(context, assignedSimAgents, stopFlag));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Message.class, this::onCommand)
                .build();
    }

    private Behavior<Message> onCommand(Message command) {
        System.out.println(command.getClass());
        System.out.println(((Message.WorkerCommand)command).dt);
        if (!stopFlag.isSet()) {
            assignedSimAgent.step(((Message.WorkerCommand)command).dt);
            ((Message.WorkerCommand)command).replyTo.tell(new Message.Response());
        }
        return Behaviors.same();
    }
}
