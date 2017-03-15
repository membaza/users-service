package com.membaza.api.users.service.captcha;

import com.fasterxml.jackson.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"success", "challenge_ts", "hostname", "error-codes" })
public final class GoogleResponse {

    private @JsonProperty("success") boolean success;
    private @JsonProperty("challenge_ts") String challengeTs;
    private @JsonProperty("hostname") String hostname;
    private @JsonProperty("error-codes") ErrorCode[] errorCodes;

    enum ErrorCode {
        MissingSecret, InvalidSecret, MissingResponse, InvalidResponse;

        private static Map<String, ErrorCode> errorsMap = new HashMap<>(4);

        static {
            errorsMap.put("missing-input-secret", MissingSecret);
            errorsMap.put("invalid-input-secret", InvalidSecret);
            errorsMap.put("missing-input-response", MissingResponse);
            errorsMap.put("invalid-input-response", InvalidResponse);
        }

        @JsonCreator
        public static ErrorCode forValue(final String value) {
            return errorsMap.get(value.toLowerCase());
        }
    }

    @JsonProperty("success")
    public boolean isSuccess() {
        return success;
    }

    @JsonProperty("success")
    public void setSuccess(boolean success) {
        this.success = success;
    }

    @JsonProperty("challenge_ts")
    public String getChallengeTs() {
        return challengeTs;
    }

    @JsonProperty("challenge_ts")
    public void setChallengeTs(String challengeTs) {
        this.challengeTs = challengeTs;
    }

    @JsonProperty("hostname")
    public String getHostname() {
        return hostname;
    }

    @JsonProperty("hostname")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @JsonProperty("error-codes")
    public void setErrorCodes(ErrorCode[] errorCodes) {
        this.errorCodes = errorCodes;
    }

    @JsonProperty("error-codes")
    public ErrorCode[] getErrorCodes() {
        return errorCodes;
    }

    @JsonIgnore
    public boolean hasClientError() {
        final ErrorCode[] errors = getErrorCodes();
        if (errors == null) {
            return false;
        }
        for (final ErrorCode error : errors) {
            switch (error) {
                case InvalidResponse:
                case MissingResponse:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "GoogleResponse{" + "success=" + success + ", challengeTs='" +
            challengeTs + '\'' + ", hostname='" + hostname + '\'' +
            ", errorCodes=" + Arrays.toString(errorCodes) + '}';
    }

}
