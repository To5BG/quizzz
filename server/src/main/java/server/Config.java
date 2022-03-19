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
import org.springframework.data.jpa.repository.JpaRepository;

@Configuration
public class Config {

    /**
     * Utility method to check if the provided id is invalid for the game session repo.
     *
     * @param id id to be checked
     * @return True iff the id is a negative integer or no entry has the provided id
     */
    public static boolean isInvalid(long id, JpaRepository repo) {
        return id < 0 || !repo.existsById(id);
    }

    /**
     * Utility method to check whether a string is empty or null.
     *
     * @param s String to be checked
     * @return True iff the object is either null or an empty string
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

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
     * "default - reset all non-persistent information
     * "all" - reset all
     * "test" - used for unit testing
     */
    @Bean
    public String updatePlayerDB() {
        return "default";
    }

}