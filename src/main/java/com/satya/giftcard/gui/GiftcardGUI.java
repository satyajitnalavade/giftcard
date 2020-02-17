package com.satya.giftcard.gui;

import com.satya.giftcard.command.IssueCommand;
import com.satya.giftcard.command.RedeemCommand;
import com.satya.giftcard.query.CardSummary;
import com.satya.giftcard.query.DataQuery;
import com.satya.giftcard.query.SizeQuery;
import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.context.annotation.Profile;

import java.util.stream.Stream;

@SpringUI
@Profile("client")
public class GiftcardGUI extends UI {

    private final CommandGateway commandGateway;
    private final DataProvider<CardSummary, Void> dataProvider;

    public GiftcardGUI(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.dataProvider = dataProvider(queryGateway);
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        HorizontalLayout commands = new HorizontalLayout();
        commands.setSizeFull();
        commands.addComponents(issuePanel(), redeemPanel());

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponents(commands, summaryGrid());
        setContent(layout);

        // Error Handler
        setErrorHandler(new DefaultErrorHandler() {
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                Throwable cause = event.getThrowable();
                while (cause.getCause() != null) cause = cause.getCause();
                Notification.show(cause.getMessage(), Notification.Type.ERROR_MESSAGE)
                        .addCloseListener(x -> dataProvider.refreshAll());
            }
        });
    }

    private Component summaryGrid() {
        Grid<CardSummary> grid = new Grid<CardSummary>();
        grid.setSizeFull();
        grid.addColumn(CardSummary::getId).setCaption("Id");
        grid.addColumn(CardSummary::getInitialBalance).setCaption("Initial Balance");
        grid.addColumn(CardSummary::getRemainingBalance).setCaption("Remaining Balance");
        grid.setDataProvider(dataProvider);
        return grid;
    }

    private Panel issuePanel() {

        TextField id = new TextField("id");
        TextField amount = new TextField("amount");
        Button submit = new Button("Submit");

        // Add listener to the button
        submit.addClickListener(event -> {
            IssueCommand cmd = new IssueCommand(id.getValue(), Integer.parseInt(amount.getValue()));

            // Sent the command to Axon
            commandGateway.sendAndWait(cmd);

            // Display a success notification
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE)
                    .addCloseListener(x -> dataProvider.refreshAll());
        });

        // Create a form and add the textfields and button
        FormLayout form = new FormLayout();
        form.setMargin(true);
        form.addComponents(id, amount, submit);
        // Add the form to the panel
        Panel panel = new Panel("Issue");
        panel.setContent(form);
        return panel;
    }

    private Panel redeemPanel() {

        TextField id = new TextField("id");
        TextField amount = new TextField("amount");
        Button submit = new Button("Submit");

        // Add listener to the button
        submit.addClickListener(event -> {
            RedeemCommand cmd = new RedeemCommand(id.getValue(), Integer.parseInt(amount.getValue()));

            // Sent the command to Axon
            commandGateway.sendAndWait(cmd);

            // Display a success notification
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE)
                    .addCloseListener(x -> dataProvider.refreshAll());
        });

        // Create a form and add the textfields and button
        FormLayout form = new FormLayout();
        form.setMargin(true);
        form.addComponents(id, amount, submit);
        // Add the form to the panel
        Panel panel = new Panel("Redeem");
        panel.setContent(form);
        return panel;
    }

    private DataProvider<CardSummary, Void> dataProvider(QueryGateway queryGateway) {
        return new AbstractBackEndDataProvider<CardSummary, Void>() {

            @Override
            protected Stream<CardSummary> fetchFromBackEnd(Query<CardSummary, Void> query) {
                return queryGateway
                        .query(
                                new DataQuery(query.getOffset(), query.getLimit()),
                                ResponseTypes.multipleInstancesOf(CardSummary.class))
                        .join()
                        .stream();
            }

            @Override
            protected int sizeInBackEnd(Query<CardSummary, Void> query) {
                return queryGateway
                        .query(new SizeQuery(), ResponseTypes.instanceOf(Integer.class))
                        .join();
            }
        };
    }
}
