package es.us.context4learning.data.api.google.firebase.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Message {

    private String to;
    @SerializedName("registration_ids")
    private String[] registrationIds;
    private String condition;
    @SerializedName("collapse_key")
    private String collapseKey;
    private String priority;
    @SerializedName("content_available")
    private boolean contentAvailable;
    @SerializedName("time_to_live")
    private Long timeToLive;
    @SerializedName("restricted_package_name")
    private String restrictedPackageName;
    @SerializedName("dry_run")
    private boolean dryRun;
    private Map<String, String> data;
    private Object notification;

    private Message(Builder builder) {
        to = builder.to;
        registrationIds = builder.registrationIds;
        condition = builder.condition;
        collapseKey = builder.collapseKey;
        priority = builder.priority;
        contentAvailable = builder.contentAvailable;
        timeToLive = builder.timeToLive;
        restrictedPackageName = builder.restrictedPackageName;
        dryRun = builder.dryRun;
        data = builder.data;
        notification = builder.notification;
    }


    public static final class Builder {
        private String to;
        private String[] registrationIds;
        private String condition;
        private String collapseKey;
        private String priority;
        private boolean contentAvailable;
        private Long timeToLive;
        private String restrictedPackageName;
        private boolean dryRun;
        private Map<String, String> data;
        private Object notification;

        public Builder() {
        }

        public Builder to(String val) {
            to = val;
            return this;
        }

        public Builder registrationIds(String[] val) {
            registrationIds = val;
            return this;
        }

        public Builder condition(String val) {
            condition = val;
            return this;
        }

        public Builder collapseKey(String val) {
            collapseKey = val;
            return this;
        }

        public Builder priority(String val) {
            priority = val;
            return this;
        }

        public Builder contentAvailable(boolean val) {
            contentAvailable = val;
            return this;
        }

        public Builder timeToLive(Long val) {
            timeToLive = val;
            return this;
        }

        public Builder restrictedPackageName(String val) {
            restrictedPackageName = val;
            return this;
        }

        public Builder dryRun(boolean val) {
            dryRun = val;
            return this;
        }

        public Builder data(Map<String, String> val) {
            data = val;
            return this;
        }

        public Builder notification(Object val) {
            notification = val;
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }
}
