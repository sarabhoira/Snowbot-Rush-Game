package com.game.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api") // this is a general api application call path
public class GameApplication extends Application {}