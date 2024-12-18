package com.example.JavaTask.services;

import com.example.JavaTask.models.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class PayloadService {

    @Value("${BATCH_SIZE}")
    private int batchSize;
    @Value("${BATCH_INTERVAL}")
    private long batchInterval;
    @Value("${POST_ENDPOINT}")
    private String postEndpointUrl;

    List<Payload> payloads = new ArrayList<>();
    private static Logger logger = LoggerFactory.getLogger(PayloadService.class);
    private ScheduledExecutorService taskScheduler;
    private ScheduledFuture<?> scheduledTask;

    private static void forwardBatch(List<Payload> payloads, String url){

        // This function executes the forwarding of the payload batch to the post endpoint mentioned in the environment variable

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<Payload>> requestEntity = new HttpEntity<>(payloads,httpHeaders);

         // This loop will try forwarding the batch upto 3 times.
        for(int i = 0; i < 3; i++) {
            try {
                long startReq = System.nanoTime();
                ResponseEntity<? extends List> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, payloads.getClass());
                long endReq = System.nanoTime();

                logger.info("Forwarded a batch of size: " + payloads.size() + " || Result status of the request: " + response.getStatusCode() + " || Duration of the post request in milliseconds: " + ((endReq - startReq) / 1000000)+"\n");
                payloads.clear();
                break;
            }
            catch (HttpClientErrorException | HttpServerErrorException e){
                logger.error("Error occured: "+e.getStatusCode()+": "+e.getMessage());
            }
            catch (RestClientException e){
                logger.error("Error occured :"+ e.getMessage());
            }

            if(i < 2){
                logger.error("Post request failed "+(i+1)+" time(s)...!! Trying again in 2 seconds...\n");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error("InterruptedException Occurred..!!\n");
                }
            }
            else{
                logger.error("Post request attempt failed 3 times..!! Could not forward Payload batch to post endpoint.\n");
            }
        }

    }

    public Payload logService(Payload payload){

        payloads.add(payload);

        logger.info("Payload received..!!\n");

        if(payloads.size() == 1){   // If a new batch is being created then forwarding is scheduled after batch interval
            taskScheduler = Executors.newScheduledThreadPool(1);
            Runnable forward = ()->forwardBatch(payloads,postEndpointUrl);
            scheduledTask = taskScheduler.schedule(forward, batchInterval, TimeUnit.MILLISECONDS);
            logger.info("First Payload in new batch is received. Batch Forwarding scheduled..!! Batch will be forwarded after "+(batchInterval/1000)+" seconds...!!\n");
        }
        else if(payloads.size() >= batchSize) { // If batch size limit is reached then batch is immediately forwarded to post endpoint.
            if (scheduledTask != null && !scheduledTask.isDone()) {
                scheduledTask.cancel(false);
                logger.info("Batch size limit reached...!! forwarding batch immediately..!!\n");
            }
            forwardBatch(payloads, postEndpointUrl);
        }

        return payload;

    }

}
