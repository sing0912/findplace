/**
 * @fileoverview 이용약관 내용 컴포넌트
 *
 * 이용약관 페이지와 회원가입 다이얼로그에서 재사용됩니다.
 * @see docs/develop/policy/frontend.md
 */

import React from 'react';
import { Box, Typography, Paper, Divider } from '@mui/material';

interface TermsOfUseContentProps {
  /** 컨테이너 래퍼 사용 여부 (다이얼로그에서는 false) */
  showContainer?: boolean;
}

/**
 * 이용약관 내용 컴포넌트
 */
const TermsOfUseContent: React.FC<TermsOfUseContentProps> = ({
  showContainer = true,
}) => {
  const content = (
    <>
      <Typography variant="h5" component="h1" gutterBottom>
        이용약관
      </Typography>
      <Typography variant="subtitle2" color="textSecondary" gutterBottom>
        시행일: 2026년 2월 7일
      </Typography>

      <Divider sx={{ my: 2 }} />

      {/* 제1조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
        제1조. 목적
      </Typography>
      <Typography variant="body2" paragraph>
        이 약관은 베뉴네트웍스(이하 "회사")가 운영하는 펫프로(PETPRO) 서비스(이하
        "서비스")의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및 책임사항, 기타
        필요한 사항을 규정함을 목적으로 합니다.
      </Typography>

      {/* 제2조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제2조. 정의
      </Typography>
      <Typography variant="body2" paragraph>
        ① "서비스"란 회사가 제공하는 AI 기반 반려동물 돌봄 플랫폼으로, 반려인과
        펫시터를 연결하여 반려동물 돌봄 예약, 돌봄 일지, 실시간 위치 트래킹, 채팅,
        결제 등의 기능을 포함하는 온라인 서비스를 말합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② "회원"이란 회사와 서비스 이용계약을 체결하고, 회원 아이디(ID)를 부여받은
        자를 말합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ③ "반려인"이란 서비스를 통해 반려동물 돌봄 서비스를 요청하는 회원을
        말합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ④ "펫시터"란 회사의 심사를 거쳐 등록된 돌봄 서비스 제공자를 말합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ⑤ "돌봄 서비스"란 펫시터가 반려인의 반려동물을 대상으로 제공하는 방문 돌봄,
        펫시터 자택 돌봄, 산책 대행 등의 서비스를 말합니다.
      </Typography>

      {/* 제3조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제3조. 약관의 효력 및 변경
      </Typography>
      <Typography variant="body2" paragraph>
        ① 이 약관은 서비스 화면에 게시하거나 기타의 방법으로 이용자에게 공지함으로써
        효력이 발생합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 회사는 필요한 경우 관련 법령을 위반하지 않는 범위 내에서 이 약관을 변경할
        수 있으며, 약관이 변경된 경우 적용일자 및 변경 사유를 명시하여 현행 약관과
        함께 서비스 내에 적용일자 7일 전부터 공지합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ③ 회원이 변경된 약관에 동의하지 않는 경우 서비스 이용을 중단하고 이용계약을
        해지할 수 있습니다. 약관 변경 공지 후 7일 이내에 거부 의사를 표시하지 않은
        경우 약관 변경에 동의한 것으로 간주합니다.
      </Typography>

      {/* 제4조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제4조. 서비스 이용 신청 및 계약 성립
      </Typography>
      <Typography variant="body2" paragraph>
        ① 서비스 이용 신청은 이용자가 회사가 정한 가입 양식에 따라 회원정보를
        기입하고, 이 약관에 동의한다는 의사표시를 함으로써 이루어집니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 이용계약은 회사가 이용 신청을 승낙하고, 회원 아이디(ID)를 부여한 시점에
        성립합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ③ 회사는 다음 각 호에 해당하는 경우 이용 신청을 승낙하지 않거나 사후에
        이용계약을 해지할 수 있습니다.
      </Typography>
      <Typography variant="body2" component="div">
        <ul>
          <li>실명이 아니거나 타인의 명의를 이용한 경우</li>
          <li>등록 내용에 허위, 기재 누락, 오기가 있는 경우</li>
          <li>서비스 운영에 지장을 초래하는 경우</li>
          <li>기타 회사가 정한 이용 요건에 충족되지 않은 경우</li>
        </ul>
      </Typography>

      {/* 제5조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제5조. 서비스 내용
      </Typography>
      <Typography variant="body2" paragraph>
        ① 회사가 제공하는 서비스의 주요 내용은 다음과 같습니다.
      </Typography>
      <Typography variant="body2" component="div">
        <ul>
          <li>펫시터 검색 및 프로필 조회</li>
          <li>반려동물 돌봄 서비스 예약 및 결제</li>
          <li>돌봄 일지 및 실시간 위치 트래킹</li>
          <li>반려인-펫시터 간 실시간 채팅</li>
          <li>후기 및 평점 시스템</li>
          <li>반려동물 프로필 관리</li>
          <li>커뮤니티 서비스</li>
          <li>기타 회사가 정하는 서비스</li>
        </ul>
      </Typography>
      <Typography variant="body2" paragraph>
        ② 회사는 서비스의 품질 향상을 위해 서비스의 내용을 변경할 수 있으며, 변경
        사항은 서비스 내 공지를 통해 안내합니다.
      </Typography>

      {/* 제6조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제6조. 이용자의 의무
      </Typography>
      <Typography variant="body2" paragraph>
        ① 이용자는 다음 각 호의 행위를 해서는 안 됩니다.
      </Typography>
      <Typography variant="body2" component="div">
        <ul>
          <li>가입 신청 또는 변경 시 허위 내용을 등록하는 행위</li>
          <li>타인의 정보를 도용하는 행위</li>
          <li>서비스를 통해 알게 된 타인의 개인정보를 무단으로 수집, 이용하는 행위</li>
          <li>서비스의 운영을 고의로 방해하는 행위</li>
          <li>반려동물에 대한 학대 또는 방임 행위</li>
          <li>펫시터와의 직거래를 유도하여 서비스를 우회하는 행위</li>
          <li>기타 관련 법령에 위반되는 행위</li>
        </ul>
      </Typography>
      <Typography variant="body2" paragraph>
        ② 반려인은 돌봄 서비스 이용 시 반려동물의 건강 상태, 성향, 주의사항 등을
        정확하게 제공할 의무가 있습니다.
      </Typography>

      {/* 제7조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제7조. 회사의 의무
      </Typography>
      <Typography variant="body2" paragraph>
        ① 회사는 관련 법령과 이 약관이 금지하거나 미풍양속에 반하는 행위를 하지 않으며,
        계속적이고 안정적으로 서비스를 제공하기 위해 최선을 다합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 회사는 이용자의 개인정보보호를 위한 보안 시스템을 갖추어야 하며, 개인정보
        처리방침을 공시하고 이를 준수합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ③ 회사는 펫시터 등록 시 자격 심사를 실시하며, 서비스 품질 유지를 위해
        지속적으로 관리·감독합니다.
      </Typography>

      {/* 제8조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제8조. 예약 및 결제
      </Typography>
      <Typography variant="body2" paragraph>
        ① 반려인은 서비스를 통해 돌봄 서비스를 예약하고, 회사가 제공하는 결제 수단을
        통해 서비스 이용 요금을 결제합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 예약 확정은 펫시터가 예약 요청을 수락하고, 반려인의 결제가 완료된 시점에
        이루어집니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ③ 서비스 이용 요금, 결제 방법, 결제 시기 등은 서비스 내에 별도로 안내합니다.
      </Typography>

      {/* 제9조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제9조. 취소 및 환불
      </Typography>
      <Typography variant="body2" paragraph>
        ① 반려인은 돌봄 서비스 시작 전에 예약을 취소할 수 있습니다. 취소 시점에 따른
        환불 규정은 다음과 같습니다.
      </Typography>
      <Typography variant="body2" component="div">
        <ul>
          <li>서비스 시작 48시간 전 취소: 전액 환불</li>
          <li>서비스 시작 24시간~48시간 전 취소: 서비스 요금의 50% 환불</li>
          <li>서비스 시작 24시간 이내 취소: 환불 불가</li>
        </ul>
      </Typography>
      <Typography variant="body2" paragraph>
        ② 펫시터의 귀책 사유로 서비스가 제공되지 못한 경우 전액 환불됩니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ③ 천재지변, 긴급 상황 등 불가피한 사유가 있는 경우 회사는 별도의 환불 기준을
        적용할 수 있습니다.
      </Typography>

      {/* 제10조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제10조. 면책사항
      </Typography>
      <Typography variant="body2" paragraph>
        ① 회사는 반려인과 펫시터 간의 돌봄 서비스 이용과 관련하여 중개 플랫폼의
        역할을 수행하며, 돌봄 서비스의 직접적인 제공 주체가 아닙니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 회사는 천재지변 또는 이에 준하는 불가항력으로 인하여 서비스를 제공할 수
        없는 경우에는 서비스 제공에 관한 책임이 면제됩니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ③ 회사는 이용자의 귀책사유로 인한 서비스 이용의 장애에 대하여 책임을 지지
        않습니다.
      </Typography>

      {/* 제11조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제11조. 분쟁해결
      </Typography>
      <Typography variant="body2" paragraph>
        ① 회사는 이용자가 제기하는 정당한 의견이나 불만을 반영하고 그 피해를 보상
        처리하기 위해 고객센터를 운영합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 회사와 이용자 간에 발생한 전자상거래 분쟁과 관련하여 이용자의 피해구제
        신청이 있는 경우에는 공정거래위원회 또는 시·도지사가 의뢰하는 분쟁조정기관의
        조정에 따를 수 있습니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ③ 이 약관에서 정하지 아니한 사항과 이 약관의 해석에 관하여는 관련 법령 또는
        상관례에 따릅니다.
      </Typography>

      {/* 제12조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제12조. 기타
      </Typography>
      <Typography variant="body2" paragraph>
        ① 이 약관은 대한민국 법률에 따라 규율되고 해석됩니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 서비스 이용으로 발생한 분쟁에 대해 소송이 제기되는 경우 회사의 본사
        소재지를 관할하는 법원을 관할 법원으로 합니다.
      </Typography>

      <Divider sx={{ my: 2 }} />

      <Typography variant="body2" align="center" color="textSecondary">
        이 이용약관은 2026년 2월 7일부터 적용됩니다.
      </Typography>
    </>
  );

  if (showContainer) {
    return (
      <Paper elevation={2} sx={{ p: 3 }}>
        {content}
      </Paper>
    );
  }

  return <Box>{content}</Box>;
};

export default TermsOfUseContent;
