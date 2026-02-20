package com.example.vibeapp.post;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.vibeapp.config.ErrorResponseDto;
import com.example.vibeapp.post.dto.*;
import com.example.vibeapp.security.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post", description = "게시글 관련 API")
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "게시글 목록 조회", description = "페이징 처리된 게시글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "429", description = "요청 제한 초과", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping
    public ResponseEntity<PostListResponseDto> list(
            @Parameter(description = "페이지 번호 (1부터 시작)") @RequestParam(value = "page", defaultValue = "1") int page) {
        int size = 5;
        List<PostListDto> posts = postService.findByPage(page, size);
        int totalPages = postService.getTotalPages(size);

        return ResponseEntity.ok(new PostListResponseDto(posts, page, totalPages));
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 번호의 게시글 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/{no}")
    public ResponseEntity<PostResponseDto> detail(@Parameter(description = "게시글 번호") @PathVariable("no") Long no) {
        PostResponseDto post = postService.getPost(no);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "게시글 등록", description = "새로운 게시글을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "등록 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping
    public ResponseEntity<Void> add(@Valid @RequestBody PostCreateDto postCreateDto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        postService.addPost(postCreateDto, securityUser.getUser().getNo());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다. 부분 업데이트(PATCH)를 지원합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (ID 불일치 등)", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PatchMapping("/{no}")
    public ResponseEntity<Void> update(@Parameter(description = "수정할 게시글 번호") @PathVariable("no") Long no,
            @Valid @RequestBody PostUpdateDto postUpdateDto,
            @AuthenticationPrincipal SecurityUser securityUser) {

        // Ensure the ID in the body matches the path parameter if it exists
        if (postUpdateDto.no() != null && !postUpdateDto.no().equals(no)) {
            throw new IllegalArgumentException("요청된 게시글 번호가 일치하지 않습니다.");
        }

        PostUpdateDto finalDto = postUpdateDto;
        if (postUpdateDto.no() == null) {
            finalDto = new PostUpdateDto(no, postUpdateDto.title(), postUpdateDto.content(), postUpdateDto.tags());
        }

        postService.modifyPost(finalDto, securityUser.getUser().getNo());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @DeleteMapping("/{no}")
    public ResponseEntity<Void> delete(@Parameter(description = "삭제할 게시글 번호") @PathVariable("no") Long no,
            @AuthenticationPrincipal SecurityUser securityUser) {
        postService.removePost(no, securityUser.getUser().getNo());
        return ResponseEntity.ok().build();
    }
}
