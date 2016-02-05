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
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;
import ru.xxlabaza.test.thrift.api.TagDto;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

/**
 *
 * @author Artem Labazin (xxlabaza)
 * @since 05.02.2016
 */
@Data
@Entity
@Table(name = "tag")
@EqualsAndHashCode(of = "name")
class Tag implements Serializable {

    private static final long serialVersionUID = -3941925483739233838L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    @ManyToMany(mappedBy = "tags",
                fetch = LAZY)
    private List<Post> posts;

    public Tag () {
    }

    public Tag (TagDto dto) {
        name = dto.getName();
    }

    TagDto toDto () {
        return new TagDto().setId(id).setName(name);
    }
}
