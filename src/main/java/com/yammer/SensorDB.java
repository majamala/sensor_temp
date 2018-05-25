package com.yammer;

import com.google.gson.Gson;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SensorDB{
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    Environment environment;
    static HashMap<Integer, SensorReading> sensorTemp = new HashMap<>();

    public SensorDB(Environment environment) {
        this.environment = environment;
    }

    public static void runTempSensor(Environment environment) throws InterruptedException{

        Config cfg = new Config();
        final Client client = new JerseyClientBuilder(environment).build("SensorRESTClient");
        client.property(ClientProperties.READ_TIMEOUT, 10000);
        Gson gson = new Gson();

        SensorInfo sensorInfo = new SensorInfo(cfg.getProperty("name"),cfg.getProperty("location"),App.ip.getHostAddress(), cfg.getProperty("port"), cfg.getProperty("description"),cfg.getProperty("unit"), cfg.getProperty("meta"));
        WebTarget webTarget = client.target ("http://localhost:8080/api/sensors");
        Response response = webTarget.request().post(Entity.json(sensorInfo));

        LOGGER.info(response.getStatusInfo().toString());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Random r = new Random();
        int rangeMin = 10;
        int rangeMax = 30;
        int id = 0;

        while (true) {

            id++;
            long now = System.currentTimeMillis();
            DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            int randomValue = r.nextInt((rangeMax - rangeMin) + 1) + rangeMin;

            SensorReading sensorReading = new SensorReading(id, "sensor_temp", format.format(now), randomValue, "'C");

            sensorTemp.put(id, sensorReading);

            webTarget = client.target ("http://localhost:8080/api/sensorReadings/sensor_temp");
            response = webTarget.request().post(Entity.json(sensorReading));

            Thread.sleep(5000);
        }
    }


    public static List<SensorReading> getSensorTemp() {
        return new ArrayList<>(sensorTemp.values());
    }
}