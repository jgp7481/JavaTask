package com.example.JavaTask.models;

import java.util.List;

public class Meta {
    private List<Login> logins;
    private PhoneNumbers phone_numbers;

        public List<Login> getLogins() {
                return logins;
        }

        public void setLogins(List<Login> logins) {
                this.logins = logins;
        }

    public PhoneNumbers getPhone_numbers() {
        return phone_numbers;
    }

    public void setPhone_numbers(PhoneNumbers phone_numbers) {
        this.phone_numbers = phone_numbers;
    }
}
