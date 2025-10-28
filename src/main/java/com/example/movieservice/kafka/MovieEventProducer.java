package com.example.movieservice.kafka;

import com.example.movieservice.event.MovieEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieEventProducer {
    
    private final KafkaTemplate<String, MovieEvent> kafkaTemplate;
    
    @Value("${kafka.topic.movie-events}")
    private String topic;
    
    public void sendMovieEvent(MovieEvent event) {
        log.info("Sending movie event: {} for movie ID: {}", event.getEventType(), event.getMovieId());
        kafkaTemplate.send(topic, String.valueOf(event.getMovieId()), event);
    }
}