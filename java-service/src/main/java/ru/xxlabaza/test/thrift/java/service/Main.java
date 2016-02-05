/*
 * Copyright 2016 xxlabaza.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.xxlabaza.test.thrift.java.service;

import javax.servlet.Servlet;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.xxlabaza.test.thrift.api.PostApi;
import ru.xxlabaza.test.thrift.api.TagApi;

/**
 *
 * @author Artem Labazin (xxlabaza)
 * @since 05.02.2016
 */
@SpringBootApplication
public class Main {

    public static void main (String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public TProtocolFactory tProtocolFactory () {
        return new TBinaryProtocol.Factory();
    }

    @Bean
    public Servlet posts (TProtocolFactory protocolFactory, PostApi.Iface service) {
        return new TServlet(new PostApi.Processor<>(service), protocolFactory);
    }

    @Bean
    public Servlet tags (TProtocolFactory protocolFactory, TagApi.Iface service) {
        return new TServlet(new TagApi.Processor<>(service), protocolFactory);
    }
}
