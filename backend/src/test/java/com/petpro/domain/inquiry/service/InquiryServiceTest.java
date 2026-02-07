package com.petpro.domain.inquiry.service;

import com.petpro.domain.inquiry.dto.InquiryRequest;
import com.petpro.domain.inquiry.dto.InquiryResponse;
import com.petpro.domain.inquiry.entity.Inquiry;
import com.petpro.domain.inquiry.entity.InquiryAnswer;
import com.petpro.domain.inquiry.entity.InquiryStatus;
import com.petpro.domain.inquiry.repository.InquiryRepository;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.entity.UserRole;
import com.petpro.domain.user.entity.UserStatus;
import com.petpro.domain.user.repository.UserRepository;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("InquiryService 테스트")
class InquiryServiceTest {

    @InjectMocks
    private InquiryService inquiryService;

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;
    private Inquiry testInquiry;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트사용자")
                .nickname("테스트닉네임")
                .phone("01012345678")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .password("encodedPassword")
                .name("다른사용자")
                .nickname("다른닉네임")
                .phone("01098765432")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        testInquiry = Inquiry.builder()
                .id(1L)
                .user(testUser)
                .title("테스트 문의")
                .content("테스트 문의 내용입니다.")
                .status(InquiryStatus.WAITING)
                .build();
        ReflectionTestUtils.setField(testInquiry, "createdAt", LocalDateTime.now());
    }

    // ==================== 문의 목록 조회 테스트 ====================

    @Nested
    @DisplayName("문의 목록 조회")
    class GetMyInquiries {

        @Test
        @DisplayName("성공: 내 문의 목록 조회")
        void getMyInquiries_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Inquiry> inquiryPage = new PageImpl<>(List.of(testInquiry), pageable, 1);
            given(inquiryRepository.findByUserIdOrderByCreatedAtDesc(anyLong(), any(Pageable.class)))
                    .willReturn(inquiryPage);

            // when
            Page<InquiryResponse.ListItem> response = inquiryService.getMyInquiries(1L, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getTitle()).isEqualTo("테스트 문의");
        }

        @Test
        @DisplayName("성공: 문의가 없는 경우 빈 목록")
        void getMyInquiries_Empty() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Inquiry> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(inquiryRepository.findByUserIdOrderByCreatedAtDesc(anyLong(), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<InquiryResponse.ListItem> response = inquiryService.getMyInquiries(1L, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
        }
    }

    // ==================== 문의 작성 테스트 ====================

    @Nested
    @DisplayName("문의 작성")
    class CreateInquiry {

        @Test
        @DisplayName("성공: 새 문의 작성")
        void createInquiry_Success() {
            // given
            InquiryRequest.Create request = InquiryRequest.Create.builder()
                    .title("새 문의")
                    .content("새 문의 내용입니다.")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(testUser));
            given(inquiryRepository.save(any(Inquiry.class))).willAnswer(invocation -> {
                Inquiry inquiry = invocation.getArgument(0);
                ReflectionTestUtils.setField(inquiry, "id", 1L);
                ReflectionTestUtils.setField(inquiry, "createdAt", LocalDateTime.now());
                return inquiry;
            });

            // when
            InquiryResponse.Detail response = inquiryService.createInquiry(1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("새 문의");
            assertThat(response.getContent()).isEqualTo("새 문의 내용입니다.");
            assertThat(response.getStatus()).isEqualTo(InquiryStatus.WAITING);
            verify(inquiryRepository).save(any(Inquiry.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자")
        void createInquiry_UserNotFound() {
            // given
            InquiryRequest.Create request = InquiryRequest.Create.builder()
                    .title("새 문의")
                    .content("새 문의 내용입니다.")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inquiryService.createInquiry(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    // ==================== 문의 상세 조회 테스트 ====================

    @Nested
    @DisplayName("문의 상세 조회")
    class GetInquiry {

        @Test
        @DisplayName("성공: 본인 문의 상세 조회")
        void getInquiry_Success() {
            // given
            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.of(testInquiry));

            // when
            InquiryResponse.Detail response = inquiryService.getInquiry(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("테스트 문의");
            assertThat(response.getContent()).isEqualTo("테스트 문의 내용입니다.");
        }

        @Test
        @DisplayName("성공: 답변이 있는 문의 조회")
        void getInquiry_WithAnswer() {
            // given
            InquiryAnswer answer = InquiryAnswer.builder()
                    .id(1L)
                    .inquiry(testInquiry)
                    .admin(otherUser)
                    .content("답변 내용입니다.")
                    .build();
            ReflectionTestUtils.setField(answer, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(testInquiry, "answer", answer);
            ReflectionTestUtils.setField(testInquiry, "status", InquiryStatus.ANSWERED);

            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.of(testInquiry));

            // when
            InquiryResponse.Detail response = inquiryService.getInquiry(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(InquiryStatus.ANSWERED);
            assertThat(response.getAnswer()).isNotNull();
            assertThat(response.getAnswer().getContent()).isEqualTo("답변 내용입니다.");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 문의")
        void getInquiry_NotFound() {
            // given
            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inquiryService.getInquiry(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INQUIRY_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 다른 사용자의 문의 조회")
        void getInquiry_AccessDenied() {
            // given
            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.of(testInquiry));

            // when & then
            assertThatThrownBy(() -> inquiryService.getInquiry(2L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }
    }

    // ==================== 문의 수정 테스트 ====================

    @Nested
    @DisplayName("문의 수정")
    class UpdateInquiry {

        @Test
        @DisplayName("성공: 문의 수정")
        void updateInquiry_Success() {
            // given
            InquiryRequest.Update request = InquiryRequest.Update.builder()
                    .title("수정된 제목")
                    .content("수정된 내용입니다.")
                    .build();

            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.of(testInquiry));

            // when
            InquiryResponse.Detail response = inquiryService.updateInquiry(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(testInquiry.getTitle()).isEqualTo("수정된 제목");
            assertThat(testInquiry.getContent()).isEqualTo("수정된 내용입니다.");
        }

        @Test
        @DisplayName("실패: 다른 사용자의 문의 수정")
        void updateInquiry_AccessDenied() {
            // given
            InquiryRequest.Update request = InquiryRequest.Update.builder()
                    .title("수정된 제목")
                    .content("수정된 내용입니다.")
                    .build();

            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.of(testInquiry));

            // when & then
            assertThatThrownBy(() -> inquiryService.updateInquiry(2L, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패: 답변 완료된 문의 수정")
        void updateInquiry_AlreadyAnswered() {
            // given
            ReflectionTestUtils.setField(testInquiry, "status", InquiryStatus.ANSWERED);

            InquiryRequest.Update request = InquiryRequest.Update.builder()
                    .title("수정된 제목")
                    .content("수정된 내용입니다.")
                    .build();

            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.of(testInquiry));

            // when & then
            assertThatThrownBy(() -> inquiryService.updateInquiry(1L, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INQUIRY_ALREADY_ANSWERED);
        }
    }

    // ==================== 문의 삭제 테스트 ====================

    @Nested
    @DisplayName("문의 삭제")
    class DeleteInquiry {

        @Test
        @DisplayName("성공: 문의 삭제")
        void deleteInquiry_Success() {
            // given
            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.of(testInquiry));

            // when
            inquiryService.deleteInquiry(1L, 1L);

            // then
            verify(inquiryRepository).delete(testInquiry);
        }

        @Test
        @DisplayName("실패: 다른 사용자의 문의 삭제")
        void deleteInquiry_AccessDenied() {
            // given
            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.of(testInquiry));

            // when & then
            assertThatThrownBy(() -> inquiryService.deleteInquiry(2L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패: 답변 완료된 문의 삭제")
        void deleteInquiry_AlreadyAnswered() {
            // given
            ReflectionTestUtils.setField(testInquiry, "status", InquiryStatus.ANSWERED);

            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.of(testInquiry));

            // when & then
            assertThatThrownBy(() -> inquiryService.deleteInquiry(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INQUIRY_ALREADY_ANSWERED);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 문의 삭제")
        void deleteInquiry_NotFound() {
            // given
            given(inquiryRepository.findByIdWithAnswer(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inquiryService.deleteInquiry(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INQUIRY_NOT_FOUND);
        }
    }
}
