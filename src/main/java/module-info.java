module io.github.wanghongzhou.hcnetsdk {

    requires java.xml;
    requires java.desktop;
    requires org.slf4j;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires spring.beans;
    requires spring.context;
    requires spring.boot.autoconfigure;
    requires jakarta.annotation;
    requires org.bytedeco.javacv;
    requires org.bytedeco.ffmpeg;
    requires org.bytedeco.opencv;
    requires io.github.wanghongzhou.javafx;
    requires static lombok;

    opens views;
    opens natives.so;
    opens natives.dll;
    opens natives.dylib;
    opens io.github.wanghongzhou.client;
    opens io.github.wanghongzhou.client.view;
    opens io.github.wanghongzhou.client.config;
    opens io.github.wanghongzhou.client.service;
    opens io.github.wanghongzhou.client.controller;
    opens io.github.wanghongzhou.hcnetsdk;

    exports io.github.wanghongzhou.client;
    exports io.github.wanghongzhou.client.view;
    exports io.github.wanghongzhou.client.mode;
    exports io.github.wanghongzhou.client.config;
    exports io.github.wanghongzhou.client.service;
    exports io.github.wanghongzhou.client.controller;
    exports io.github.wanghongzhou.hcnetsdk;
}