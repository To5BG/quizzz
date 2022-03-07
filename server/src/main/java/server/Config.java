/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package server;

import java.util.Random;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    /**
     * Configure a random object for the game session
     *
     * @return Return a new object Random
     */
    @Bean
    public Random getRandom() {
        return new Random();
    }

    /**
     * Configuration for resetting the database.
     *
     * @return one of options
     * Options:
     * "sessions" - reset sessions
     * "player" - reset players
     * "all" - both players and game sessions
     * "test" - used for unit testing
     */
    @Bean
    public String updatePlayerDB() {
        return "sessions";
    }

}