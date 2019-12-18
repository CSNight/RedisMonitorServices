package csnight.redis.monitor.exception;

public class CmdMsgException extends Exception {
    public CmdMsgException() {
    }

    public CmdMsgException(String msg) {
        super("Command msg Error:" + msg);
    }

    public CmdMsgException(String msg, Throwable t) {
        super(msg, t);
    }
}
