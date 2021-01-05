package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PushCheckResult {
    public String result;
    public MiPushInformation miPush;
    @JsonProperty("firebase")
    public FirebasePushInformation fcm;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public int uid,groupId;
    public String groupTitle;
    public boolean groupAllowed;

    public static class MiPushInformation{
        public boolean enabled;
    }

    public static class FirebasePushInformation{
        public boolean enabled;
    }
}
