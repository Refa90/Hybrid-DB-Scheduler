class ReminderWrapper{
    static int jobIndex = 1;
    static int completedJobs = 0;

    static int fatQueriesCount = 0;
    static int thinQueriesCount = 0;

    public ReminderWrapper(int delay, int duration, int amount, boolean useLearning, boolean state){
        int interval = duration / amount;
        for (int i = 0; i < amount; i++){
            new Reminder(delay + interval * i,useLearning, jobIndex++, state );
        }
    }
}