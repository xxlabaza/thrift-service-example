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

import java.util.Set;
import lombok.val;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.xxlabaza.test.thrift.api.InvalidTagException;
import ru.xxlabaza.test.thrift.api.TagApi;
import ru.xxlabaza.test.thrift.api.TagDto;
import ru.xxlabaza.test.thrift.api.UnexistingTagException;

import static java.util.stream.Collectors.toSet;

/**
 *
 * @author Artem Labazin (xxlabaza)
 * @since 05.02.2016
 */
@Service
@Transactional(readOnly = true)
class TagService implements TagApi.Iface {

    @Autowired
    private TagRepository tagRepository;

    @Override
    @Transactional
    public TagDto create (TagDto tag) throws InvalidTagException, TException {
        try {
            return tagRepository.save(new Tag(tag)).toDto();
        } catch (Throwable ex) {
            throw new InvalidTagException(tag, ex.getMessage());
        }
    }

    @Override
    public TagDto find (long id) throws UnexistingTagException, TException {
        val tag = findTag(id);
        return tag.toDto();
    }

    @Override
    public Set<TagDto> findAll () throws TException {
        return tagRepository.findAll().stream().map(Tag::toDto).collect(toSet());
    }

    @Override
    @Transactional
    public void remove (long id) throws UnexistingTagException, TException {
        val tag = findTag(id);
        tagRepository.delete(tag);
    }

    private Tag findTag (long id) throws UnexistingTagException {
        val tag = tagRepository.findOne(id);
        if (tag == null) {
            throw new UnexistingTagException(id, String.format("There is no such Tag, with id - %d", id));
        }
        return tag;
    }
}
