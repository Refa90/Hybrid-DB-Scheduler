
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.deploy.net.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


class Learning {
    Timer timer;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Learning(int time, int delay){
        Timer timer = new Timer();
        timer.schedule(new LearningTask(time), delay *1000);
    }

    private String sendPostLearning(int time, int aCount, int bCount) throws Exception {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead

        Map<String, Object> map = new HashMap();

        map.put("runningTime", time);
        map.put("aCount", aCount);
        map.put("bCount", bCount);

        String value = new ObjectMapper().writeValueAsString(map);

        try {

            HttpPost request = new HttpPost("http://127.0.0.1:3000/predict");
            StringEntity params = new StringEntity(value);
            request.addHeader("Content-Type", "application/json");
            request.setEntity(params);
            CloseableHttpResponse response = httpClient.execute(request);

            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");

            return responseString;

            //handle response here...

        } catch (Exception ex) {
            System.out.println(ex);

        }

        return null;
    }


    class LearningTask extends TimerTask{

        private int time;

        public LearningTask(int time){
            this.time = time;
        }

        public void run() {
            String tech = null;

            int currentValueFat = ReminderWrapper.fatQueriesCount;
            int currentValueThin = ReminderWrapper.thinQueriesCount;

            try {
                Date date = new Date();
                System.out.println(String.format("%s - learning: %s, %s, %s",dateFormat.format(date), this.time, currentValueFat, currentValueThin));

                tech = sendPostLearning(this.time, currentValueFat, currentValueThin);

                date = new Date();
                System.out.println(String.format("%s - learning response: %s",dateFormat.format(date), tech));
            } catch (Exception e) {
                e.printStackTrace();
            }

            ReminderWrapper.fatQueriesCount -= currentValueFat;
            ReminderWrapper.thinQueriesCount -= currentValueThin;

            if(tech.equals("T1")){
                Reminder.port = 3001;
            }else{
                Reminder.port = 3002;
            }
        }
    }
}


public class Reminder {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    Timer timer;
    static int port;

    String fatWine = "{\r\n    \"name0\": \"CHATEAU DE SAINT COSME\",\r\n    \"year0\": \"2009\",\r\n    \"grapes0\": \"Grenache / Syrah\",\r\n    \"country0\": \"France\",\r\n    \"region0\": \"Southern Rhone\",\r\n    \"description0\": \"The aromas of fruit and spice give one a hint of the light drinkability of this lovely wine, which makes an excellent complement to fish dishes.\",\r\n    \"picture0\": \"saint_cosme.jpg\",\r\n    \"name1\": \"CHATEAU DE SAINT COSME\",\r\n    \"year1\": \"2009\",\r\n    \"grapes1\": \"Grenache / Syrah\",\r\n    \"country1\": \"France\",\r\n    \"region1\": \"Southern Rhone\",\r\n    \"description1\": \"The aromas of fruit and spice give one a hint of the light drinkability of this lovely wine, which makes an excellent complement to fish dishes.\",\r\n    \"picture1\": \"saint_cosme.jpg\",\r\n    \"name2\": \"CHATEAU DE SAINT COSME\",\r\n    \"year2\": \"2009\",\r\n    \"grapes2\": \"Grenache / Syrah\",\r\n    \"country2\": \"France\",\r\n    \"region2\": \"Southern Rhone\",\r\n    \"description2\": \"The aromas of fruit and spice give one a hint of the light drinkability of this lovely wine, which makes an excellent complement to fish dishes.\",\r\n    \"picture2\": \"saint_cosme.jpg\",\r\n    \"name3\": \"CHATEAU DE SAINT COSME\",\r\n    \"year3\": \"2009\",\r\n    \"grapes3\": \"Grenache / Syrah\",\r\n    \"country3\": \"France\",\r\n    \"region3\": \"Southern Rhone\",\r\n    \"description3\": \"The aromas of fruit and spice give one a hint of the light drinkability of this lovely wine, which makes an excellent complement to fish dishes.\",\r\n    \"picture3\": \"saint_cosme.jpg\"\r\n  }";
    String thinWine = "{\r\n    \"name\": \"CHATEAU DE SAINT COSME\",\r\n    \"year\": \"2009\",\r\n    \"grapes\": \"Grenache / Syrah\",\r\n    \"country\": \"France\",\r\n    \"region\": \"Southern Rhone\",\r\n    \"description\": \"The aromas of fruit and spice give one a hint of the light drinkability of this lovely wine, which makes an excellent complement to fish dishes.\",\r\n    \"picture\": \"saint_cosme.jpg\"\r\n  }";

    public Reminder(int delay, boolean useLearning, int index, boolean state) {
        timer = new Timer();
        timer.schedule(new RemindTask(Reminder.port, useLearning, index, state), delay * 1000);
    }


    class RemindTask extends TimerTask {

        int port;
        boolean useLeearning;
        int index;
        boolean state;

        public RemindTask(int port, boolean useLearning, int index, boolean state){
            this.port = port;
            this.useLeearning = useLearning;
            this.index = index;
            this.state = state;
        }
        public void run() {

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            System.out.println(String.format("%s - index: %s - Time's Start!",dateFormat.format(date), index));

            String wine = getWine(state);

            if(state){
                ReminderWrapper.fatQueriesCount++;
            }else{
                ReminderWrapper.thinQueriesCount++;
            }

            date = new Date();
            System.out.println(String.format("%s - index: %s - state: %s",dateFormat.format(date), index, state));

            try {
                sendPost(Reminder.port, wine);
            } catch (Exception e) {
                e.printStackTrace();
            }

            date = new Date();
            System.out.println(String.format("%s - index: %s - done",dateFormat.format(date), index));
            timer.cancel(); //Terminate the timer thread

            ReminderWrapper.completedJobs++;
        }
    }

    public static void main(String args[]) throws InterruptedException {
        System.out.println("About to schedule task.");
        Reminder.port = 3001;

        int[] distArray={12,2,6,1,3,3,1,4,5,5,6,2,4,4,2,8,9,1,7,3,5,1,3,10,12,5,5,4,7,2};
        int deleayBetweenQueries=60;
        int duration=60;
        int delay;
        int j=0;
        boolean useLearning=true;
        for(int i=0;i<distArray.length/2;i++){
            delay=deleayBetweenQueries*i;
            new ReminderWrapper(delay, duration, distArray[j], useLearning, true);
            new ReminderWrapper(delay, duration, distArray[++j], useLearning, false);
            new Learning(i + 1,delay);
            j++;
        }

        System.out.println("Task scheduled.");

        while(ReminderWrapper.completedJobs < ReminderWrapper.jobIndex - 1){
            Thread.sleep(1000);
        }

        System.out.println("exit");
    }

    private String getWine(boolean state){
        if(state){
            return fatWine;
        }else{
            return thinWine;
        }
    }

    private HttpResponse sendPost(int port, String wine) throws Exception {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead

        try {

            HttpPost request = new HttpPost("http://193.106.55.134:" + String.valueOf(port) + "/getwines/");
            StringEntity params = new StringEntity(wine);
            request.addHeader("Content-Type", "application/json");
            request.setEntity(params);
            HttpResponse response = (HttpResponse) httpClient.execute(request);

            return response;

            //handle response here...

        } catch (Exception ex) {
            System.out.println(ex);

        }

        return null;
    }


}