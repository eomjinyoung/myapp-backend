package com.example.vibeapp.post;

import com.example.vibeapp.post.dto.PostCreateDto;
import com.example.vibeapp.post.dto.PostListDto;
import com.example.vibeapp.post.dto.PostResponseDto;
import com.example.vibeapp.post.dto.PostUpdateDto;
import com.example.vibeapp.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final com.example.vibeapp.user.UserRepository userRepository;

    public PostService(PostRepository postRepository, PostTagRepository postTagRepository,
            com.example.vibeapp.user.UserRepository userRepository) {
        this.postRepository = postRepository;
        this.postTagRepository = postTagRepository;
        this.userRepository = userRepository;
    }

    public List<PostListDto> findByPage(int page, int size) {
        // PageRequest와 Sort를 사용하여 페이징 및 정렬 처리
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "no"));
        return postRepository.findAll(pageRequest).getContent().stream()
                .map(PostListDto::from)
                .collect(Collectors.toList());
    }

    public int getTotalPages(int size) {
        long total = postRepository.count();
        return (int) Math.ceil((double) total / size);
    }

    @Transactional
    public PostResponseDto getPost(Long id) {
        // Optional을 반환하는 findById()를 사용함
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            // 변경 감지(Dirty Checking): 영속 상태의 엔티티 값을 변경하면 트랜잭션 종료 시점에 DB에 반영됨
            post.setViews(post.getViews() + 1);

            List<PostTag> tags = postTagRepository.findByPostNo(id);
            String tagsString = tags.stream()
                    .map(PostTag::getTagName)
                    .collect(Collectors.joining(", "));
            return PostResponseDto.from(post, tagsString);
        }
        return null;
    }

    @Transactional
    public void addPost(PostCreateDto createDto, Long authorNo) {
        Post post = createDto.toEntity();
        User author = userRepository.findById(authorNo).orElse(null);
        post.setAuthor(author);
        post.setCreatedAt(java.time.LocalDateTime.now());
        post.setUpdatedAt(null);
        post.setViews(0);
        postRepository.save(post);

        saveTags(post.getNo(), createDto.tags());
    }

    public void checkOwnership(Long postNo, Long userNo) {
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (post.getAuthor() == null || !post.getAuthor().getNo().equals(userNo)) {
            throw new IllegalArgumentException("본인의 게시글만 수정/삭제할 수 있습니다.");
        }
    }

    @Transactional
    public void modifyPost(PostUpdateDto updateDto, Long userNo) {
        checkOwnership(updateDto.no(), userNo);
        // Dirty Checking(변경 감지)을 활용하여 필요한 필드만 업데이트함.
        Post post = postRepository.findById(updateDto.no()).orElse(null);
        if (post != null) {
            post.setTitle(updateDto.title());
            post.setContent(updateDto.content());
            post.setUpdatedAt(java.time.LocalDateTime.now());

            postTagRepository.deleteByPostNo(post.getNo());
            saveTags(post.getNo(), updateDto.tags());
        }
    }

    private void saveTags(Long postNo, String tagsString) {
        if (tagsString != null && !tagsString.isBlank()) {
            java.util.Arrays.stream(tagsString.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .distinct()
                    .forEach(tagName -> {
                        PostTag tag = new PostTag();
                        tag.setPostNo(postNo);
                        tag.setTagName(tagName);
                        postTagRepository.save(tag);
                    });
        }
    }

    @Transactional
    public void removePost(Long id, Long userNo) {
        checkOwnership(id, userNo);
        postTagRepository.deleteByPostNo(id);
        postRepository.deleteById(id);
    }
}
