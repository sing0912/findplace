package com.petpro.domain.inquiry.service;

import com.petpro.domain.inquiry.dto.InquiryRequest;
import com.petpro.domain.inquiry.dto.InquiryResponse;
import com.petpro.domain.inquiry.entity.Inquiry;
import com.petpro.domain.inquiry.repository.InquiryRepository;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.repository.UserRepository;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 문의 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    /**
     * 내 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<InquiryResponse.ListItem> getMyInquiries(Long userId, Pageable pageable) {
        return inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(InquiryResponse.ListItem::from);
    }

    /**
     * 문의 작성
     */
    @Transactional
    public InquiryResponse.Detail createInquiry(Long userId, InquiryRequest.Create request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return InquiryResponse.Detail.from(savedInquiry);
    }

    /**
     * 문의 상세 조회
     */
    @Transactional(readOnly = true)
    public InquiryResponse.Detail getInquiry(Long userId, Long inquiryId) {
        Inquiry inquiry = findInquiryById(inquiryId);

        // 본인 문의인지 확인
        if (!inquiry.isOwner(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return InquiryResponse.Detail.from(inquiry);
    }

    /**
     * 문의 수정
     */
    @Transactional
    public InquiryResponse.Detail updateInquiry(Long userId, Long inquiryId, InquiryRequest.Update request) {
        Inquiry inquiry = findInquiryById(inquiryId);

        // 본인 문의인지 확인
        if (!inquiry.isOwner(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 수정 가능 여부 확인
        if (!inquiry.isModifiable()) {
            throw new BusinessException(ErrorCode.INQUIRY_ALREADY_ANSWERED);
        }

        inquiry.update(request.getTitle(), request.getContent());
        return InquiryResponse.Detail.from(inquiry);
    }

    /**
     * 문의 삭제
     */
    @Transactional
    public void deleteInquiry(Long userId, Long inquiryId) {
        Inquiry inquiry = findInquiryById(inquiryId);

        // 본인 문의인지 확인
        if (!inquiry.isOwner(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 삭제 가능 여부 확인
        if (!inquiry.isModifiable()) {
            throw new BusinessException(ErrorCode.INQUIRY_ALREADY_ANSWERED);
        }

        inquiryRepository.delete(inquiry);
    }

    /**
     * ID로 문의 조회 (내부 메서드)
     */
    private Inquiry findInquiryById(Long id) {
        return inquiryRepository.findByIdWithAnswer(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
    }
}
