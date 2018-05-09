package uk.ac.ebi.pride.ws.pride.utils;

import org.springframework.http.HttpStatus;

/**
 * @author ypriverol
 */
public class ErrorInfo {

    public final int code;
    public final String reason;
    public final String url;
    public String[] messages = null;

    public ErrorInfo(HttpStatus status, StringBuffer url, String... messages) {
        this.code = status.value();
        this.reason = status.name();
        this.url = url.toString();
        if (messages != null && messages.length > 0) {
            this.messages = messages;
        }
    }

}
