package com.devil.blindmail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SMTPAuthenticator extends Authenticator {

    private String username,password;

    SMTPAuthenticator(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        if ((username != null) && (username.length() > 0) && (password != null)
                && (password.length() > 0)) {
            return new PasswordAuthentication(username, password);
        }

        return null;
    }
}