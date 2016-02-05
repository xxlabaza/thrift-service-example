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

import java.util.Arrays;
import java.util.HashSet;
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
import ru.xxlabaza.test.thrift.api.PostApi;
import ru.xxlabaza.test.thrift.api.PostDto;
import ru.xxlabaza.test.thrift.api.TagApi;
import ru.xxlabaza.test.thrift.api.TagDto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Artem Labazin (xxlabaza)
 * @since 05.02.2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class PostServiceTest {

    @Autowired
    private TProtocolFactory protocolFactory;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagApi.Iface tagService;

    @Value("${local.server.port}")
    private int port;

    private PostApi.Iface client;

    @Before
    public void before () throws Exception {
        val transport = new THttpClient("http://localhost:" + port + "/posts/");
        val protocol = protocolFactory.getProtocol(transport);
        client = new PostApi.Client(protocol);

        postRepository.deleteAllInBatch();
    }

    @Test
    public void create () throws Exception {
        val name = "my_post";
        val post = client.create(new PostDto(name));

        assertNotNull(post);
        assertNotEquals(0, post.getId());
        assertEquals(name, post.getName());
        assertNull(post.getTags());
    }

    @Test
    public void find () throws Exception {
        val name = "my_post";
        val postId = client.create(new PostDto(name)).getId();

        val post = client.find(postId);
        assertNotNull(post);
        assertEquals(postId, post.getId());
        assertEquals(name, post.getName());
        assertNull(post.getTags());
    }

    @Test
    public void findAll () throws Exception {
        client.create(new PostDto("first"));
        client.create(new PostDto("second"));
        client.create(new PostDto("third"));

        val allPosts = client.findAll();
        assertNotNull(allPosts);
        assertEquals(3, allPosts.size());
    }

    @Test
    public void findAllByTag () throws Exception {
        val tagOne = tagService.create(new TagDto("tag_one")).getId();
        val tagTwo = tagService.create(new TagDto("tag_two")).getId();

        val postFirst = client.create(new PostDto("first")).getId();
        val postSecond = client.create(new PostDto("second")).getId();
        val postThird = client.create(new PostDto("third")).getId();

        client.addTag(postFirst, tagOne);
        client.addTag(postSecond, tagOne);
        client.addTag(postThird, tagTwo);

        val result1 = client.findAllByTag(tagOne);
        assertNotNull(result1);
        assertEquals(2, result1.size());
        assertTrue(result1.stream().map(PostDto::getId).allMatch(id -> id == postFirst || id == postSecond));

        val result2 = client.findAllByTag(tagTwo);
        assertNotNull(result2);
        assertEquals(1, result2.size());
        assertEquals(postThird, result2.iterator().next().getId());
    }

    @Test
    public void findAllByTags () throws Exception {
        val tagOne = tagService.create(new TagDto("tag_one")).getId();
        val tagTwo = tagService.create(new TagDto("tag_two")).getId();
        val tagThree = tagService.create(new TagDto("tag_three")).getId();

        val postFirst = client.create(new PostDto("first")).getId();
        val postSecond = client.create(new PostDto("second")).getId();
        val postThird = client.create(new PostDto("third")).getId();

        client.addTag(postFirst, tagOne);
        client.addTag(postSecond, tagTwo);
        client.addTag(postThird, tagThree);

        val result = client.findAllByTags(new HashSet<>(Arrays.asList(tagOne, tagThree)));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().map(PostDto::getId).allMatch(id -> id == postFirst || id == postThird));
    }

    @Test
    public void addTag () throws Exception {
        val tag = tagService.create(new TagDto("tag_one")).getId();
        val post = client.create(new PostDto("first")).getId();
        client.addTag(post, tag);

        val result = client.find(post);
        assertNotNull(result);

        val tags = result.getTags();
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertTrue(tags.stream().map(TagDto::getId).allMatch(id -> id == tag));
    }

    @Test
    public void addTags () throws Exception {
        val tagOne = tagService.create(new TagDto("tag_one")).getId();
        val tagTwo = tagService.create(new TagDto("tag_two")).getId();
        val post = client.create(new PostDto("first")).getId();
        client.addTags(post, new HashSet<>(Arrays.asList(tagOne, tagTwo)));

        val result = client.find(post);
        assertNotNull(result);

        val tags = result.getTags();
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.stream().map(TagDto::getId).allMatch(id -> id == tagOne || id == tagTwo));
    }

    @Test
    public void removeTag () throws Exception {
        val tagOne = tagService.create(new TagDto("tag_one")).getId();
        val tagTwo = tagService.create(new TagDto("tag_two")).getId();
        val post = client.create(new PostDto("first")).getId();
        client.addTags(post, new HashSet<>(Arrays.asList(tagOne, tagTwo)));
        assertEquals(2, client.find(post).getTags().size());

        client.removeTag(post, tagTwo);

        val result = client.find(post);
        assertNotNull(result);

        val tags = result.getTags();
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertTrue(tags.stream().map(TagDto::getId).allMatch(id -> id == tagOne));
    }

    @Test
    public void removeTags () throws Exception {
        val tagOne = tagService.create(new TagDto("tag_one")).getId();
        val tagTwo = tagService.create(new TagDto("tag_two")).getId();
        val post = client.create(new PostDto("first")).getId();
        client.addTags(post, new HashSet<>(Arrays.asList(tagOne, tagTwo)));
        assertEquals(2, client.find(post).getTags().size());

        client.removeTags(post, new HashSet<>(Arrays.asList(tagOne, tagTwo)));

        val result = client.find(post);
        assertNotNull(result);

        val tags = result.getTags();
        assertNull(tags);
    }

    @Test
    public void getAllTags () throws Exception {
        val tagOne = tagService.create(new TagDto("tag_one")).getId();
        val tagTwo = tagService.create(new TagDto("tag_two")).getId();
        val post = client.create(new PostDto("first")).getId();
        client.addTags(post, new HashSet<>(Arrays.asList(tagOne, tagTwo)));

        val result = client.getAllTags(post);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().map(TagDto::getId).allMatch(id -> id == tagOne || id == tagTwo));
    }

    @Test
    public void remove () throws Exception {
        val id = client.create(new PostDto("my_post")).getId();
        assertEquals(1, client.findAll().size());

        client.remove(id);
        assertEquals(0, client.findAll().size());
    }
}
