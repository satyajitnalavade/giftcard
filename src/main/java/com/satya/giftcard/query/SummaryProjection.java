package com.satya.giftcard.query;

import com.satya.giftcard.command.IssuedEvent;
import com.satya.giftcard.command.RedeemedEvent;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;

@Component
@Profile("query")
public class SummaryProjection {
    private final EntityManager entityManager;

    public SummaryProjection(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventHandler
    public void handle(IssuedEvent evt) {
        entityManager.persist(new CardSummary(evt.getId(), evt.getAmount(), evt.getAmount()));
    }

    // Configure Tracking Event
    @Autowired
    public void config(EventProcessingConfiguration config) {}

    @EventHandler
    public void handle(RedeemedEvent evt) {
        CardSummary summary = entityManager.find(CardSummary.class, evt.getId());
        summary.setRemainingBalance(summary.getRemainingBalance() - evt.getAmount());
    }

    @QueryHandler
    public List<CardSummary> handle(DataQuery query) {
        return entityManager
                .createQuery("SELECT c FROM CardSummary c ORDER BY c.id", CardSummary.class)
                .getResultList();
    }

    @QueryHandler
    public Integer handle(SizeQuery query) {
        return entityManager
                .createQuery("SELECT COUNT(c) FROM CardSummary c", Long.class)
                .getSingleResult()
                .intValue();
    }
}
