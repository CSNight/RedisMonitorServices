package csnight.redis.monitor.quartz.jobs;

import csnight.redis.monitor.websocket.WebSocketServer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class JobInstance implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("ssdsss");
        WebSocketServer.getInstance().broadcast("ssss");
    }
}
