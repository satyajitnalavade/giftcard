# Giftcard

This is a demo application built using CQRS architecture pattern. 
CQRS stands for The Command Query Responsibility Segregation. 
CQRS pattern seperates read and update operations for a data store.

In this demo application for Giftcard aggregate there are just two events 

issued: a new gift card gets created with some amount of money stored.
redeemed: all or part of the monetary value stored on the gift card is used to purchase something.

All business logic / rules are defined in the @CommandHandlers, 
and all state changes are defined in the @EventSourcingHandlers. 
The reason for this is when we want to get the current state of event-sourced aggregate, 
we have to apply all sourced events - we have to invoke @EventSourcingHandlers. 


