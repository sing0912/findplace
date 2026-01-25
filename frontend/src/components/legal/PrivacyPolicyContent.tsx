/**
 * @fileoverview 개인정보처리방침 내용 컴포넌트
 *
 * 개인정보처리방침 페이지와 회원가입 다이얼로그에서 재사용됩니다.
 */

import React from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Divider,
} from '@mui/material';

interface PrivacyPolicyContentProps {
  /** 컨테이너 래퍼 사용 여부 (다이얼로그에서는 false) */
  showContainer?: boolean;
}

/**
 * 개인정보처리방침 내용 컴포넌트
 */
const PrivacyPolicyContent: React.FC<PrivacyPolicyContentProps> = ({
  showContainer = true,
}) => {
  const content = (
    <>
      <Typography variant="h5" component="h1" gutterBottom>
        개인정보처리방침
      </Typography>
      <Typography variant="subtitle2" color="textSecondary" gutterBottom>
        시행일: 2026년 1월 25일
      </Typography>

      <Divider sx={{ my: 2 }} />

      {/* 제1조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
        제1조. 목적
      </Typography>
      <Typography variant="body2" paragraph>
        ① 이 방침은 『개인정보보호법』, 『정보통신망 이용촉진 및 정보보호에 관한 법률』상의
        개인정보보호규정 및 방송통신위원회가 제정한 『개인정보보호지침』을 준수하여 이용자의
        개인정보를 보호하고 이와 관련한 고충을 신속하고 원활하게 처리함으로써 베뉴네트웍스(이하
        "회사")가 제공하는 파인드플레이스(FINDPLACE) 서비스(이하 "서비스") 이용자들의
        개인정보보호와 고충처리에 이바지함을 목적으로 합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 개인정보처리방침은 정부의 법령 또는 지침이 변경되었을 때, 또는 보다 나은 서비스의
        제공을 위하여 그 내용이 변경될 수 있습니다. 이 경우 회사는 웹사이트 공지사항 또는
        이메일을 통하여 전달함을 원칙으로 합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ③ 회사는 이용자의 개인정보보호를 위하여, 개인정보처리방침을 사이트 첫 화면의 연결화면을
        통해 공개하고, 이용자가 언제든지 볼 수 있도록 조치하고 있습니다.
      </Typography>

      {/* 제2조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제2조. 개인정보의 수집이용 목적 및 수집항목
      </Typography>
      <Typography variant="body2" paragraph>
        ① 회사는 회원가입, 견적상담, 서비스 제공 등을 위해 다음과 같이 개인정보를 수집하고
        있습니다. 이용자가 개인정보 수집 항목의 동의 버튼을 클릭하거나, 회원정보 수정 등을 통해
        추가로 수집하는 개인정보를 입력한 후 저장할 경우 개인정보 수집에 동의한 것으로
        간주합니다.
      </Typography>

      <Typography variant="subtitle2" gutterBottom sx={{ mt: 2 }}>
        [ 개인정보 수집 항목 - 이용자 ]
      </Typography>
      <TableContainer component={Paper} variant="outlined" sx={{ mb: 2 }}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>분류</TableCell>
              <TableCell>수집·이용 목적</TableCell>
              <TableCell>수집·이용 항목</TableCell>
              <TableCell>보유·이용 기간</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            <TableRow>
              <TableCell>필수</TableCell>
              <TableCell>회원가입</TableCell>
              <TableCell>이메일, 비밀번호, 닉네임</TableCell>
              <TableCell rowSpan={6}>
                회원탈퇴 및 수집 목적 달성 시까지. 단, 관계법령의 규정에 따라 보존할 필요가 있는
                경우를 제외
              </TableCell>
            </TableRow>
            <TableRow>
              <TableCell>필수</TableCell>
              <TableCell>간편가입</TableCell>
              <TableCell>SNS ID, 닉네임, 프로필사진, 이메일 등</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>필수</TableCell>
              <TableCell>본인인증</TableCell>
              <TableCell>이름, 핸드폰 번호</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>필수</TableCell>
              <TableCell>장례 신청 및 상담</TableCell>
              <TableCell>이름, 핸드폰 번호, 장례 픽업 신청 주소</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>필수</TableCell>
              <TableCell>상품구매 및 배송</TableCell>
              <TableCell>이름, 이메일, 핸드폰 번호, 주소, 결제정보</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>필수</TableCell>
              <TableCell>환불(취소)</TableCell>
              <TableCell>계좌번호, 은행명, 예금주</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </TableContainer>

      <Typography variant="body2" paragraph>
        ② 회사는 이용자의 개인정보에 대하여 사전동의 없이 동의 범위를 초과하여 이용하거나,
        원칙적으로 이용자의 개인정보를 타인 또는 타기업, 기관에 제공하지 않습니다.
      </Typography>

      {/* 제3조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제3조. 개인정보 제3자 제공 및 위탁 내용
      </Typography>
      <Typography variant="body2" paragraph>
        ① 제3자에게 이용자의 개인정보 제공이 필요한 경우, 사전에 이용자에게 '제공 목적, 제공받는
        자, 제공하는 개인정보 항목, 제공받는 개인정보 보유 및 이용기간'을 고지하고 이에 대해
        별도 동의를 구하여 동의를 받은 경우, 회사는 제3자에 이용자의 개인정보를 제공 및 위탁할
        수 있습니다.
      </Typography>

      <Typography variant="subtitle2" gutterBottom sx={{ mt: 2 }}>
        [ 개인정보 수탁 및 위탁 항목 ]
      </Typography>
      <TableContainer component={Paper} variant="outlined" sx={{ mb: 2 }}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>위탁 업무 내용</TableCell>
              <TableCell>수탁 업체</TableCell>
              <TableCell>보유 및 이용기간</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            <TableRow>
              <TableCell>데이터 보관 및 처리 등 국내 클라우드 서버 운영 및 관리</TableCell>
              <TableCell>카페24(주)</TableCell>
              <TableCell>회원탈퇴 시 혹은 위탁계약 종료시까지</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </TableContainer>

      {/* 제4조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제4조. 개인정보의 수집 방법
      </Typography>
      <Typography variant="body2" component="div">
        회사는 다음의 방법으로 개인정보를 수집합니다.
        <ul>
          <li>서비스 홈페이지, 서면 양식, 전화/팩스, 이벤트 응모</li>
          <li>개인정보 제공 동의를 받은 제휴사</li>
          <li>생성정보 수집 툴(쿠키)을 통한 수집</li>
          <li>기타 기술적 방법을 통한 수집</li>
        </ul>
      </Typography>

      {/* 제5조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제5조. 개인정보의 처리 및 보유 기간
      </Typography>
      <Typography variant="body2" paragraph>
        ① 회사는 이용자로부터 개인정보를 수집할 때 동의 받은 개인정보 보유·이용기간 또는 법령에
        따른 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 회사가 가입 시 제공하는 할인쿠폰, 이벤트 혜택 등을 여러번 취하기 위하여 탈퇴 후
        재가입 등을 반복적으로 행하거나 이 과정에서 타인의 명의를 무단으로 사용하는 편법과
        불법행위를 하는 회원을 차단하고자, 휴대전화 인증 정보는 회원 탈퇴 후 6개월 동안
        보관합니다.
      </Typography>

      <Typography variant="subtitle2" gutterBottom sx={{ mt: 2 }}>
        [ 법령에 의해 수집 이용되는 항목 ]
      </Typography>
      <TableContainer component={Paper} variant="outlined" sx={{ mb: 2 }}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>보유 정보</TableCell>
              <TableCell>보유기간</TableCell>
              <TableCell>근거 법령</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            <TableRow>
              <TableCell>계약 또는 청약철회 등에 관한 기록</TableCell>
              <TableCell>5년</TableCell>
              <TableCell>전자상거래 등에서의 소비자보호에 관한 법률</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>대금결제 및 재화 등의 공급에 관한 기록</TableCell>
              <TableCell>5년</TableCell>
              <TableCell>전자상거래 등에서의 소비자보호에 관한 법률</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>소비자의 불만 또는 분쟁처리에 관한 기록</TableCell>
              <TableCell>3년</TableCell>
              <TableCell>전자상거래 등에서의 소비자보호에 관한 법률</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>표시, 광고에 관한 기록</TableCell>
              <TableCell>6개월</TableCell>
              <TableCell>전자상거래 등에서의 소비자보호에 관한 법률</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>웹사이트 방문 기록</TableCell>
              <TableCell>3개월</TableCell>
              <TableCell>통신비밀보호법</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </TableContainer>

      {/* 제6조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제6조. 개인정보의 파기
      </Typography>
      <Typography variant="body2" paragraph>
        ① 회사는 원칙적으로 개인정보 처리목적이 달성된 경우에는 지체없이 해당 개인정보를
        파기합니다. 파기의 절차, 기한 및 방법은 다음과 같습니다.
      </Typography>
      <Typography variant="body2" component="div">
        <ul>
          <li>
            <strong>파기절차:</strong> 이용자가 입력한 정보는 목적 달성 후 별도의 DB에 옮겨져
            내부 방침 및 기타 관련 법령에 따라 일정기간 저장된 후 혹은 즉시 파기됩니다.
          </li>
          <li>
            <strong>파기기한:</strong> 이용자의 개인정보는 개인정보의 보유기간이 경과된 경우에는
            보유기간의 종료일로부터 5일 이내에 파기합니다.
          </li>
          <li>
            <strong>파기방법:</strong> 전자적 파일 형태의 정보는 기록을 재생할 수 없도록 기술적
            방법을 사용하여 파기합니다. 종이에 출력된 개인정보는 분쇄기로 분쇄하거나 소각을
            통하여 파기합니다.
          </li>
        </ul>
      </Typography>

      {/* 제7조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제7조. 이용자 및 법정대리인 권리의무와 그 행사방법
      </Typography>
      <Typography variant="body2" paragraph>
        ① 이용자 및 법정대리인은 등록되어 있는 본인 혹은 당해 피대리인의 개인정보를 열람하거나
        정정할 수 있습니다. 개인정보 열람 및 정정을 하고자 할 경우에는 파인드플레이스 서비스의
        '마이페이지' {'>'} '수정'을 클릭하여 직접 열람 또는 수정하거나, 고객센터를 통하여 문의 시
        회사는 신속하게 처리될 수 있도록 최선의 노력을 합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 이용자는 회원가입 등을 통해 개인정보의 수집, 이용, 제공에 대해 동의한 내용을
        고객센터를 통해 언제든지 철회할 수 있습니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ③ 이용자는 서비스의 '마이페이지 {'>'} 수정 {'>'} 회원탈퇴'를 통해 직접 탈퇴를 진행하거나,
        고객센터를 통하여 회사에 의사를 전달하면 회사는 개인정보의 삭제 등 필요한 조치를 취한 후
        지체 없이 통지합니다.
      </Typography>

      {/* 제8조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제8조. 개인정보의 안전성 확보조치
      </Typography>
      <Typography variant="body2" paragraph>
        회사는 이용자의 개인정보를 처리함에 있어 정보의 분실, 도난, 누출, 외부로부터의 공격,
        해킹 등으로 훼손되지 않도록 안전성을 확보하기 위하여 기술적·관리적 및 물리적 조치를 하고
        있으며, 개인정보의 안전한 처리를 위하여 휴면고객 정책 운영 등 내부관리 계획을
        수립·시행하고 있습니다.
      </Typography>
      <Typography variant="body2" component="div">
        <ul>
          <li>개인정보 취급자의 최소화 및 교육</li>
          <li>개인정보에 대한 접근 제한</li>
          <li>접속기록의 보관 및 위변조 방지</li>
          <li>개인정보의 암호화</li>
          <li>해킹 등에 대비한 기술적 대책</li>
        </ul>
      </Typography>

      {/* 제9조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제9조. 개인정보 처리방침 변경 및 예외
      </Typography>
      <Typography variant="body2" paragraph>
        ① 이 개인정보처리방침은 시행일로부터 적용되며, 법령 및 방침에 따른 변경 내용의 추가,
        삭제 및 정정이 있는 경우에는 변경사항의 시행 7일 전부터 공지사항을 통하여 고지합니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 회사의 서비스에서 이용자에게 제공되는 다른 서비스의 링크 또는 외부 자료의 경우 회사의
        '개인정보처리방침'이 적용되지 않습니다.
      </Typography>

      {/* 제10조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제10조. 개인정보 자동 수집 장치의 설치/운영 및 거부에 관한 사항
      </Typography>
      <Typography variant="body2" paragraph>
        ① 회사는 이용자의 정보를 수시로 저장하고 찾아내는 쿠키(cookie) 등 개인정보를 자동으로
        수집하는 장치를 설치, 운용할 수 있습니다. 쿠키의 사용 목적은 이용자 접속 빈도나 방문
        시간 등을 분석을 통한 타겟 마케팅 및 개인 맞춤 서비스 제공에 있습니다.
      </Typography>
      <Typography variant="body2" paragraph>
        ② 이용자는 쿠키 설치에 대한 선택권을 가지고 있습니다. 웹 브라우저에서 옵션을 설정함으로써
        모든 쿠키를 허용하거나 거부할 수 있으며, 쿠키 거부 시 서비스 제공에 어려움이 있을 수
        있습니다.
      </Typography>

      {/* 제11조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제11조. 개인정보 보호책임자
      </Typography>
      <Typography variant="body2" paragraph>
        ① 회사는 개인정보 처리에 관한 업무를 총괄하여 책임지고, 개인정보 처리 관련 이용자의
        불만처리 및 피해구제 등을 위하여 아래와 같이 개인정보 보호책임자를 지정하고 있습니다.
      </Typography>
      <Box sx={{ bgcolor: 'grey.100', p: 2, borderRadius: 1, mb: 2 }}>
        <Typography variant="body2">
          <strong>개인정보 보호책임자:</strong> 우상철
        </Typography>
        <Typography variant="body2">
          <strong>연락처:</strong> 010-4865-0569
        </Typography>
        <Typography variant="body2">
          <strong>이메일:</strong> help@findplace.kr
        </Typography>
      </Box>

      {/* 제12조 */}
      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        제12조. 권익침해 구제 방법
      </Typography>
      <Typography variant="body2" paragraph>
        이용자는 개인정보 침해에 대한 피해구제, 상담 등을 아래의 기관 등에 문의할 수 있습니다.
      </Typography>
      <Typography variant="body2" component="div">
        <ul>
          <li>개인정보 침해신고센터 (privacy.kisa.or.kr): (국번없이) 118</li>
          <li>개인정보 분쟁조정위원회 (www.kopico.go.kr): 1833-6972</li>
          <li>대검찰청 사이버수사과 (spo.go.kr): 1301</li>
          <li>경찰청 사이버안전지킴이: 182</li>
        </ul>
      </Typography>

      <Divider sx={{ my: 2 }} />

      <Typography variant="body2" align="center" color="textSecondary">
        이 개인정보 처리방침은 2026년 1월 25일부터 적용됩니다.
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

export default PrivacyPolicyContent;
