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

import lombok.val;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.THttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.xxlabaza.test.thrift.api.TagApi;
import ru.xxlabaza.test.thrift.api.TagDto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Artem Labazin (xxlabaza)
 * @since 05.02.2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class TagServiceTest {

    @Autowired
    private TProtocolFactory protocolFactory;

    @Autowired
    private TagRepository tagRepository;

    @Value("${local.server.port}")
    private int port;

    private TagApi.Client client;

    @Before
    public void before () throws Exception {
        val transport = new THttpClient("http://localhost:" + port + "/tags/");
        val protocol = protocolFactory.getProtocol(transport);
        client = new TagApi.Client(protocol);

        tagRepository.deleteAllInBatch();
    }

    @Test
    public void create () throws Exception {
        val name = "my_tag";
        val tag = client.create(new TagDto().setName(name));

        assertNotNull(tag);
        assertNotEquals(0, tag.getId());
        assertEquals(name, tag.getName());
        assertNull(tag.getPosts());
    }

    @Test
    public void find () throws Exception {
        val name = "my_tag";
        val tagId = client.create(new TagDto().setName(name)).getId();

        val tag = client.find(tagId);
        assertNotNull(tag);
        assertEquals(tagId, tag.getId());
        assertEquals(name, tag.getName());
        assertNull(tag.getPosts());
    }

    @Test
    public void findAll () throws Exception {
        client.create(new TagDto().setName("first"));
        client.create(new TagDto().setName("second"));
        client.create(new TagDto().setName("third"));

        val allTags = client.findAll();
        assertNotNull(allTags);
        assertEquals(3, allTags.size());
    }

    @Test
    public void remove () throws Exception {
        val tagId = client.create(new TagDto().setName("my_tag")).getId();
        assertEquals(1, client.findAll().size());

        client.remove(tagId);
        assertEquals(0, client.findAll().size());
    }
}
