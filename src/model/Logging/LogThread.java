package model.Logging;

public class LogThread extends Thread {

    private LogEntry _entry;

    public LogThread(String name, LogEntry entry){
        super(name);
        _entry = entry;
    }

    public void run(){
        LoggerQueue.Resource.enqueue(_entry);
        LoggerQueue.Resource.processLog();
    }
}
