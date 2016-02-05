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

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Wither;
import lombok.val;
import org.hibernate.validator.constraints.NotEmpty;
import ru.xxlabaza.test.thrift.api.PostDto;

import static java.util.stream.Collectors.toSet;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

/**
 *
 * @author Artem Labazin (xxlabaza)
 * @since 05.02.2016
 */
@Data
@Entity
@Wither
@Table(name = "post")
@EqualsAndHashCode(of = "name")
class Post implements Serializable {

    private static final long serialVersionUID = -237470943341847120L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name = "post_to_tag",
            joinColumns = {
                @JoinColumn(name = "post",
                            referencedColumnName = "id")
            },
            inverseJoinColumns = {
                @JoinColumn(name = "tag",
                            referencedColumnName = "id")
            }
    )
    private List<Tag> tags;

    public Post () {
    }

    public Post (PostDto dto) {
        name = dto.getName();
    }

    public Post (Long id, String name, List<Tag> tags) {
        this.id = id;
        this.name = name;
        this.tags = tags;
    }

    PostDto toDto () {
        val tagsDto = tags == null || tags.isEmpty()
                      ? null
                      : tags.stream().map(Tag::toDto).collect(toSet());
        return new PostDto().setId(id).setName(name).setTags(tagsDto);
    }
}
