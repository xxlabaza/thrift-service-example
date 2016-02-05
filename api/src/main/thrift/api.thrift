namespace java ru.xxlabaza.test.thrift.api


typedef i64 PostId
typedef i64 TagId


struct PostDto {
    1: optional PostId id,
    2: required string name,
    3: optional set<TagDto> tags,
}

struct TagDto {
    1: optional TagId id,
    2: required string name,
    3: optional set<PostDto> posts,
}


exception InvalidPostException {
    1: PostDto post,
    2: string message
}

exception InvalidTagException {
    1: TagDto tag,
    2: string message
}

exception UnexistingPostException {
    1: PostId id,
    2: string message
}

exception UnexistingTagException {
    1: TagId id,
    2: string message
}


service PostApi {

    PostDto create (1:PostDto post) throws (1:InvalidPostException ex),

    PostDto find (1: PostId id) throws (1:UnexistingPostException ex),
    
    set<PostDto> findAll (),

    set<PostDto> findAllByTag (1: TagId tag) throws (1: UnexistingTagException ex),

    set<PostDto> findAllByTags (1: set<TagId> tags) throws (1: UnexistingTagException ex),

    void addTag (1: PostId post, 2: TagId tag) throws (1:UnexistingPostException upe, 2: UnexistingTagException ute),

    void addTags (1: PostId post, 2: set<TagId> tags) throws (1:UnexistingPostException upe, 2: UnexistingTagException ute),

    void removeTag (1: PostId post, 2: TagId tag) throws (1:UnexistingPostException upe, 2: UnexistingTagException ute),

    void removeTags (1: PostId post, 2: set<TagId> tags) throws (1:UnexistingPostException upe, 2: UnexistingTagException ute),

    set<TagDto> getAllTags (1: PostId post) throws (1:UnexistingPostException ex),

    void remove (1: PostId id) throws (1:UnexistingPostException ex)
}

service TagApi {

    TagDto create (1: TagDto tag) throws (1: InvalidTagException ex),

    set<TagDto> findAll (),

    TagDto find (1: TagId id) throws (1: UnexistingTagException ex),

    void remove (1: TagId id) throws (1: UnexistingTagException ex)
}

