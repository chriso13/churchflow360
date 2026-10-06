package com.churchflow360.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.*;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitMQConfig {

    // Exchange — all churchflow360 events route through this
    public static final String EXCHANGE = "churchflow360.events";

    // Queue names
    public static final String Q_PRAYER   = "churchflow360.prayer.request.created";
    public static final String Q_WELCOME  = "churchflow360.member.registered";
    public static final String Q_PASTORAL = "churchflow360.pastoral.meeting.scheduled";
    public static final String Q_ANNOUNCE = "churchflow360.announcement.published";
    public static final String Q_TITHE    = "churchflow360.finance.tithe.recorded";

    @Bean
    public TopicExchange churchflowExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean public Queue prayerQueue()   { return QueueBuilder.durable(Q_PRAYER).build(); }
    @Bean public Queue welcomeQueue()  { return QueueBuilder.durable(Q_WELCOME).build(); }
    @Bean public Queue pastoralQueue() { return QueueBuilder.durable(Q_PASTORAL).build(); }
    @Bean public Queue announceQueue() { return QueueBuilder.durable(Q_ANNOUNCE).build(); }
    @Bean public Queue titheQueue()    { return QueueBuilder.durable(Q_TITHE).build(); }

    @Bean public Binding prayerBinding()   { return BindingBuilder.bind(prayerQueue()).to(churchflowExchange()).with("prayer.request.created"); }
    @Bean public Binding welcomeBinding()  { return BindingBuilder.bind(welcomeQueue()).to(churchflowExchange()).with("member.registered"); }
    @Bean public Binding pastoralBinding() { return BindingBuilder.bind(pastoralQueue()).to(churchflowExchange()).with("pastoral.meeting.scheduled"); }
    @Bean public Binding announceBinding() { return BindingBuilder.bind(announceQueue()).to(churchflowExchange()).with("announcement.published"); }
    @Bean public Binding titheBinding()    { return BindingBuilder.bind(titheQueue()).to(churchflowExchange()).with("finance.tithe.recorded"); }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
