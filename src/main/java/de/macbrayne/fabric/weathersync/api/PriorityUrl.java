package de.macbrayne.fabric.weathersync.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public record PriorityUrl(URI url, int priority) {

    private static final Logger LOGGER = LoggerFactory.getLogger("weathersync");

    public static PriorityUrl of(String url, int priority) {
        return new PriorityUrl(URI.create(url), priority);
    }

    public boolean isAvailable() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url.toString() + "?latitude=0&longitude=0&current=weather_code&timezone=GMT"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                LOGGER.info("Found responsive backend, priority " + priority);
                return true;
            }
        } catch (Exception e) {
            LOGGER.debug("Couldn't find backend ", e);
        }
        return false;
    }
}
