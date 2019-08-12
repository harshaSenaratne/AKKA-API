package com.stocks;

import com.stocks.UserRegistryActor.User;
import com.stocks.UserRegistryActor.InitUser;

import java.io.Serializable;
import akka.actor.ActorRef;


public interface UserRegistryMessages {

    class GetUsers implements Serializable {
    }

    class ActionPerformed implements Serializable {
        private final String description;

        public ActionPerformed(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

    }

    class CreateUser implements Serializable {
        private final InitUser user;

        public CreateUser(InitUser user) {
            this.user = user;
        }

        public InitUser getUser() {
            return user;
        }
    }

    class GetUser implements Serializable {
        private final String name;

        public GetUser(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    class DeleteUser implements Serializable {
        private final String name;

        public DeleteUser(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    class CreatedUser implements Serializable{
        private final User user;

        public CreatedUser(User user){
            this.user=user;
        }
        public User getUser() {
            return user;
        }   
    }
}