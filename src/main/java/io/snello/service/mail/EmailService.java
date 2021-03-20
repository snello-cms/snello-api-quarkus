package io.snello.service.mail;

public interface EmailService {

    void send(Email email) throws Exception;
}
