package com.example.vibeapp.post;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Spring Data JPA를 사용한 게시글 태그 리포지토리 인터페이스.
 */
public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    /**
     * 특정 게시글 번호에 해당하는 모든 태그를 조회함.
     * Query Method 규칙에 따라 자동으로 SQL 생성됨.
     */
    List<PostTag> findByPostNo(Long postNo);

    /**
     * 특정 게시글 번호에 해당하는 모든 태그를 삭제함.
     * Query Method 규칙에 따라 자동으로 SQL 생성됨.
     */
    void deleteByPostNo(Long postNo);
}
