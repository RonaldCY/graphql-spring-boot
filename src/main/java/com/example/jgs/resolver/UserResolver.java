package com.example.jgs.resolver;

import com.example.jgs.input.CreateUser;
import com.example.jgs.model.User;
import com.example.jgs.service.UserService;
import io.leangen.graphql.annotations.*;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Controller;
import io.leangen.graphql.spqr.spring.util.ConcurrentMultiMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class UserResolver {

    private final UserService userService;
    private final ConcurrentMultiMap<String, FluxSink<User>> subscribers = new ConcurrentMultiMap<>();

    @GraphQLQuery
    @GraphQLNonNull
    public List<@GraphQLNonNull User> users() {
        return this.userService.find();
    }

    @GraphQLQuery
    public User user(
            @GraphQLArgument(name = "id") UUID id) {
        return this.userService.findOneById(id);
    }

    @GraphQLMutation
    @GraphQLNonNull
    public User createUser(
            @GraphQLArgument(name = "input") @GraphQLNonNull CreateUser createUser) {
        User user = this.userService.create(createUser);
        //Notify all the subscribers following this task
        subscribers.get("create").forEach(subscriber -> subscriber.next(user));
        return user;
    }

    @GraphQLMutation
    public UUID deleteUser(
            @GraphQLArgument(name = "id") UUID id) {
        User user = this.userService.findOneById(id);
        subscribers.get("delete").forEach(subscriber -> subscriber.next(user));
        return this.userService.delete(id) ? id : null;
    }

    @GraphQLSubscription
    public Publisher<User> notification(String code) {
        return Flux.create(subscriber -> subscribers.add(code, subscriber.onDispose(() -> subscribers.remove(code, subscriber))), FluxSink.OverflowStrategy.LATEST);
    }
}