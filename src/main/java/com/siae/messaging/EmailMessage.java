package com.siae.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class EmailMessage {

    private String to;
    private String subject;
    private String body;
    private String username;
    private String templateName;
}
