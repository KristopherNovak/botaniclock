package com.krisnovak.springboot.demo.planttracker.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Class that represents the body of an HTTP Response
 */
public class HTTPResponseBody {
    //The HTTP Status being conveyed
    private int status;
    //The message of the body
    private String message;
    //The time that the HTTP response body was generated
    private long timeStamp;

    //Users should use newInstance to generate HTTPResponseBody
    private HTTPResponseBody(){}

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "AccountResponseBody{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }

    private HTTPResponseBody(HTTPResponseBody.HTTPResponseBodyBuilder builder){
        this.status = builder.status;
        this.message = builder.message;
        this.timeStamp = builder.timeStamp;
    }

    public static class HTTPResponseBodyBuilder{
        private int status;
        private String message;
        private long timeStamp;

        public HTTPResponseBody.HTTPResponseBodyBuilder setStatus(int status){
            this.status = status;
            return this;
        }

        public HTTPResponseBody.HTTPResponseBodyBuilder setMessage(String message){
            this.message = message;
            return this;
        }

        public HTTPResponseBody.HTTPResponseBodyBuilder setTimeStamp(long timeStamp){
            this.timeStamp = timeStamp;
            return this;
        }

        public HTTPResponseBody build(){
            return new HTTPResponseBody(this);
        }
    }

    /**
     * Function that creates a new instance of an HTTPResponseBody
     * @param httpStatus The HTTP Status to be conveyed in the HTTP response including the HTTPResponseBody
     * @param message The message that the HTTPResponseBody should convey
     * @return
     */
    public static HTTPResponseBody newInstance(HttpStatus httpStatus, String message){
        return new HTTPResponseBody.HTTPResponseBodyBuilder()
                .setStatus(httpStatus.value())
                .setMessage(message)
                .setTimeStamp(System.currentTimeMillis())
                .build();
    }
}
