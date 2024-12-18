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
    private int batchInterval;
    @Value("${POST_ENDPOINT}")
    private String postEndpointUrl;

    List<Payload> payloads = new ArrayList<>();
    private static Logger logger = LoggerFactory.getLogger(PayloadService.class);
    private ScheduledExecutorService taskScheduler;
    private ScheduledFuture<?> scheduledTask;

    private static void forwardBatch(List<Payload> payloads, String url){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<Payload>> requestEntity = new HttpEntity<>(payloads,httpHeaders);

        for(int i = 0; i < 3; i++) {
            try {
                long startReq = System.nanoTime();
                ResponseEntity<? extends List> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, payloads.getClass());
                long endReq = System.nanoTime();

                logger.info("Forwarded a batch of size: " + payloads.size() + " || Result status of the request: " + response.getStatusCode() + " || Duration of the post request in milliseconds: " + ((endReq - startReq) / 1000000));
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
                logger.error("Post request failed "+(i+1)+" time(s)...!! Trying again in 2 seconds...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error("InterruptedException Occurred..!!");
                }
            }
            else{
                logger.error("Post request attempt failed 3 times..!! Could not forward Payload batch to post endpoint.");
            }
        }

        System.out.println("Length of the list : "+ payloads.size());
        System.out.println("\n\n"+payloads);
//        System.out.println("\n\nList of payloads at the receiver : "+response);

    }

    public Payload logService(Payload payload){

        payloads.add(payload);

        logger.info("Payload received..!!");

        if(payloads.size() == 1){
            taskScheduler = Executors.newScheduledThreadPool(1);
            Runnable forward = ()->forwardBatch(payloads,postEndpointUrl);
            scheduledTask = taskScheduler.schedule(forward, batchInterval, TimeUnit.MILLISECONDS);
            System.out.println("Forwarding scheduled..!! Batch will be forwarded after 10 seconds...!!\n\n");
        }
        else if(payloads.size() >= batchSize) {
            if (scheduledTask != null && !scheduledTask.isDone()) {
                scheduledTask.cancel(false);
                System.out.println("Batch size limit reached...!! forwarding batch immediately..\n\n");
                logger.info("Batch size limit reached...!! forwarding batch immediately..!!");
            }
            forwardBatch(payloads, postEndpointUrl);
        }

        return payload;

    }

}
