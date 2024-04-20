package com.handson.tinyurl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handson.tinyurl.model.NewTinyRequest;
import com.handson.tinyurl.model.User;
import com.handson.tinyurl.model.UserClickOut;
import com.handson.tinyurl.repository.UserClickRepository;
import com.handson.tinyurl.repository.UserRepository;
import com.handson.tinyurl.service.Redis;
import com.handson.tinyurl.util.Dates;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.handson.tinyurl.model.User.UserBuilder.anUser;
import org.springframework.data.mongodb.core.query.Query;
import static org.springframework.data.util.StreamUtils.createStreamFromIterator;
import static com.handson.tinyurl.model.UserClick.UserClickBuilder.anUserClick;
import static com.handson.tinyurl.model.UserClickKey.UserClickKeyBuilder.anUserClickKey;

@RestController
public class AppController {

    private static final int MAX_RETRIES = 4;
    private static final int TINY_LENGTH = 6;
    Random random = new Random();

    @Autowired
    Redis redis;

    @Autowired
    ObjectMapper om;
    @Value("${base.url}")
    String baseUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserClickRepository userClickRepository;

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public User createUser(@RequestParam String name) {
        User user = anUser().withName(name).build();
        user = userRepository.insert(user);
        return user;
    }

    @CrossOrigin
    @RequestMapping(value = "/user/{name}", method = RequestMethod.GET)
    public User getUser(@RequestParam String name) {
        User user = userRepository.findFirstByName(name);
        return user;
    }

    private void incrementMongoField(String userName, String key) {
        Query query = Query.query(Criteria.where("name").is(userName));
        Update update = new Update().inc(key, 1);
        mongoTemplate.updateFirst(query, update, "users");
    }

    private void setMongoLongUrl(String userName, String longUrl, String key) {
        Query query = Query.query(Criteria.where("name").is(userName));
        Update update = new Update().set(key, longUrl);
        mongoTemplate.updateFirst(query, update, "users");
    }

    @RequestMapping(value = "/{tiny}/", method = RequestMethod.GET)
    public ModelAndView getTiny(@PathVariable String tiny) throws JsonProcessingException {
        Object tinyRequestStr = redis.get(tiny);
        NewTinyRequest tinyRequest = om.readValue(tinyRequestStr.toString(), NewTinyRequest.class);
        if (tinyRequest.getLongUrl() != null) {
            String userName = tinyRequest.getUserName();
            if (userName != null) {
                incrementMongoField(userName, "allUrlClicks");
                incrementMongoField(userName, "shorts." + tiny + ".totalClicks");
                // setMongoLongUrl(userName, tinyRequest.getLongUrl(), "shorts." + tiny + ".longUrl");
                incrementMongoField(userName,
                        "shorts." + tiny + ".clicks." + Dates.getCurMonth());
                userClickRepository.save(anUserClick()
                        .userClickKey(anUserClickKey().withUserName(userName).withClickTime(new Date()).build())
                        .tiny(tiny).longUrl(tinyRequest.getLongUrl()).build());
            }
            return new ModelAndView("redirect:" + tinyRequest.getLongUrl());
        } else {
            throw new RuntimeException(tiny + " not found");
        }
    }

    @CrossOrigin
    @RequestMapping(value = "/tiny", method = RequestMethod.POST)
    public String generate(@RequestBody NewTinyRequest request) throws JsonProcessingException {
        String tinyCode = generateTinyCode();
        int i = 0;
        while (!redis.set(tinyCode, om.writeValueAsString(request)) && i < MAX_RETRIES) {
            tinyCode = generateTinyCode();
            i++;
        }
        if (i == MAX_RETRIES)
            throw new RuntimeException("SPACE IS FULL");


        //create user if not exists:
        User user = userRepository.findFirstByName("noam");
        // User user = getUser(request.getUserName());
        if (user == null){
            user = anUser().withName("noam").build();
            user = userRepository.insert(user);
        }

        //add new new url to mongo
        Object tinyRequestStr = redis.get(tinyCode);
        NewTinyRequest tinyRequest = om.readValue(tinyRequestStr.toString(), NewTinyRequest.class);
        String userName = tinyRequest.getUserName();
        setMongoLongUrl(userName, tinyRequest.getLongUrl(), "shorts." + tinyCode + ".longUrl");

        return baseUrl + tinyCode + "/";
    }

    private String generateTinyCode() {
        String charPool = "ABCDEFHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < TINY_LENGTH; i++) {
            res.append(charPool.charAt(random.nextInt(charPool.length())));
        }
        return res.toString();
    }

    @RequestMapping(value = "/user/{name}/clicks", method = RequestMethod.GET)
    public List<UserClickOut> getUserClicks(@RequestParam String name) {
        var userClicks = createStreamFromIterator(userClickRepository.findByUserName(name).iterator())
                .map(userClick -> UserClickOut.of(userClick))
                .collect(Collectors.toList());
        return userClicks;
    }

}
