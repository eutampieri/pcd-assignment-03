package pcd.ass03.ass01.simengine_conc;
import akka.actor.typed.ActorRef;

public class Message {


    public static class Command extends Message{
        public final int numSteps;

        public Command(int numSteps) {
            this.numSteps = numSteps;
        }
    }

    public static class Response extends Message {
    }

    public static class WorkerCommand extends Message {
        public final int dt;
        public final ActorRef<Message> replyTo;

        public WorkerCommand(int dt, ActorRef<Message> replyTo) {
            this.dt = dt;
            this.replyTo = replyTo;
        }
    }
}
