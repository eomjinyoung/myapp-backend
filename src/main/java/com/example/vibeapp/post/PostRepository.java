package com.example.vibeapp.post;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA를 사용한 게시글 리포지토리 인터페이스.
 * JpaRepository를 상속받아 기본적인 CRUD 및 페이징 기능을 자동으로 제공받음.
 */
public interface PostRepository extends JpaRepository<Post, Long> {
    // 추가적인 쿼리 메서드가 필요한 경우 여기에 정의 (예: findByTitle)
}
