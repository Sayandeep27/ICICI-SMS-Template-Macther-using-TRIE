package com.sms.matcher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sms_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsTemplate {

    @Id
    private Long id;

    // Example: "Dear Customer,your OTP is {#var},valid for {#var} mins."
    @Column(name = "template_text", nullable = false, unique = true)
    private String templateText;
}
