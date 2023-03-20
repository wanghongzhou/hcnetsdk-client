package io.github.wanghongzhou.client.mode;

import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Repository;

/**
 * @author Brian
 */
@Repository
public class LoginInfo {

    private final SimpleStringProperty ip = new SimpleStringProperty(this, "ip", "192.168.60.164");
    private final SimpleStringProperty user = new SimpleStringProperty(this, "user", "admin");
    private final SimpleStringProperty password = new SimpleStringProperty(this, "password", "JL654321");
    private final SimpleStringProperty port = new SimpleStringProperty(this, "port", "8000");

    public String getIp() {
        return this.ip.get();
    }

    public void setIp(String value) {
        this.ip.set(value);
    }

    public SimpleStringProperty ipProperty() {
        return this.ip;
    }

    public String getUser() {
        return this.user.get();
    }

    public void setUser(String value) {
        this.user.set(value);
    }

    public SimpleStringProperty userProperty() {
        return this.user;
    }

    public String getPassword() {
        return this.password.get();
    }

    public void setPassword(String value) {
        this.password.set(value);
    }

    public SimpleStringProperty passwordProperty() {
        return this.password;
    }

    public String getPort() {
        return this.port.get();
    }

    public void setPort(String value) {
        this.port.set(value);
    }

    public SimpleStringProperty portProperty() {
        return this.port;
    }
}
