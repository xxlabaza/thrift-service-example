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
import java.util.Set;
import lombok.val;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.xxlabaza.test.thrift.api.InvalidPostException;
import ru.xxlabaza.test.thrift.api.PostApi;
import ru.xxlabaza.test.thrift.api.PostDto;
import ru.xxlabaza.test.thrift.api.TagDto;
import ru.xxlabaza.test.thrift.api.UnexistingPostException;
import ru.xxlabaza.test.thrift.api.UnexistingTagException;

import static java.util.stream.Collectors.toSet;

/**
 *
 * @author Artem Labazin (xxlabaza)
 * @since 05.02.2016
 */
@Service
@Transactional(readOnly = true)
class PostService implements PostApi.Iface {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Override
    @Transactional
    public PostDto create (PostDto post) throws InvalidPostException, TException {
        try {
            return postRepository.save(new Post(post)).toDto();
        } catch (Throwable ex) {
            throw new InvalidPostException(post, ex.getMessage());
        }
    }

    @Override
    public PostDto find (long id) throws UnexistingPostException {
        return postRepository.findOne(id).toDto();
    }

    @Override
    public Set<PostDto> findAll () throws TException {
        return postRepository.findAll().stream().map(Post::toDto).collect(toSet());
    }

    @Override
    public Set<PostDto> findAllByTag (long tag) throws UnexistingTagException, TException {
        val tagDomain = tagRepository.findOne(tag);
        return postRepository.findAllByTagsIn(Arrays.asList(tagDomain)).stream().map(Post::toDto).collect(toSet());
    }

    @Override
    public Set<PostDto> findAllByTags (Set<Long> tags) throws UnexistingTagException, TException {
        val tagDomains = tagRepository.findAll(tags);
        return postRepository.findAllByTagsIn(tagDomains).stream().map(Post::toDto).collect(toSet());
    }

    @Override
    @Transactional
    public void addTag (long post, long tag) throws UnexistingPostException, UnexistingTagException, TException {
        val tagDomain = tagRepository.findOne(tag);
        val postDomain = findPost(post);
        postDomain.getTags().add(tagDomain);
    }

    @Override
    @Transactional
    public void addTags (long post, Set<Long> tags) throws UnexistingPostException, UnexistingTagException, TException {
        val tagDomains = tagRepository.findAll(tags);
        val postDomain = findPost(post);
        postDomain.getTags().addAll(tagDomains);
    }

    @Override
    @Transactional
    public void removeTag (long post, long tag) throws UnexistingPostException, UnexistingTagException, TException {
        val tagDomain = tagRepository.findOne(tag);
        val postDomain = findPost(post);
        postDomain.getTags().remove(tagDomain);
    }

    @Override
    @Transactional
    public void removeTags (long post, Set<Long> tags) throws UnexistingPostException, UnexistingTagException,
                                                              TException {
        val tagDomains = tagRepository.findAll(tags);
        val postDomain = findPost(post);
        postDomain.getTags().removeAll(tagDomains);
    }

    @Override
    @Transactional
    public Set<TagDto> getAllTags (long id) throws UnexistingPostException, TException {
        val domain = findPost(id);
        return domain.getTags().stream().map(Tag::toDto).collect(toSet());
    }

    @Override
    @Transactional
    public void remove (long id) throws UnexistingPostException, TException {
        val post = findPost(id);
        postRepository.delete(post);
    }

    Post findPost (long id) throws UnexistingPostException {
        val post = postRepository.findOne(id);
        if (post == null) {
            throw new UnexistingPostException(id, String.format("There is no such Post, with id - %d", id));
        }
        return post;
    }
}
