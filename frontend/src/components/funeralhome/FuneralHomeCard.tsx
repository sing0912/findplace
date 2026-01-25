/**
 * @fileoverview 장례식장 카드 컴포넌트
 */

import React from 'react';
import type { FuneralHomeListItem } from '../../types/funeralHome';

interface FuneralHomeCardProps {
  /** 장례식장 정보 */
  funeralHome: FuneralHomeListItem;
  /** 클릭 핸들러 */
  onClick?: (id: number) => void;
  /** 거리 표시 여부 */
  showDistance?: boolean;
}

/**
 * 장례식장 카드 컴포넌트
 */
const FuneralHomeCard: React.FC<FuneralHomeCardProps> = ({
  funeralHome,
  onClick,
  showDistance = true,
}) => {
  const handleClick = () => {
    if (onClick) {
      onClick(funeralHome.id);
    }
  };

  const handlePhoneClick = (e: React.MouseEvent) => {
    e.stopPropagation();
  };

  return (
    <div
      className="funeral-home-card"
      onClick={handleClick}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          handleClick();
        }
      }}
    >
      <div className="funeral-home-card__header">
        <h3 className="funeral-home-card__name">{funeralHome.name}</h3>
        {showDistance && funeralHome.distance !== undefined && (
          <span className="funeral-home-card__distance">
            {funeralHome.distance.toFixed(1)}km
          </span>
        )}
      </div>

      <p className="funeral-home-card__address">{funeralHome.roadAddress}</p>

      {funeralHome.phone && (
        <a
          href={`tel:${funeralHome.phone}`}
          className="funeral-home-card__phone"
          onClick={handlePhoneClick}
        >
          📞 {funeralHome.phone}
        </a>
      )}

      <div className="funeral-home-card__services">
        {funeralHome.hasCrematorium && (
          <span className="funeral-home-card__service-tag funeral-home-card__service-tag--crematorium">
            화장
          </span>
        )}
        {funeralHome.hasColumbarium && (
          <span className="funeral-home-card__service-tag funeral-home-card__service-tag--columbarium">
            납골
          </span>
        )}
        {funeralHome.hasFuneral && (
          <span className="funeral-home-card__service-tag funeral-home-card__service-tag--funeral">
            장례
          </span>
        )}
      </div>

      <p className="funeral-home-card__region">{funeralHome.locName}</p>
    </div>
  );
};

export default FuneralHomeCard;
