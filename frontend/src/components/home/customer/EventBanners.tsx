import React, { useState, useEffect, useCallback } from 'react';
import { Box, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { EventBanner } from '../../../types/home';

interface EventBannersProps {
  banners: EventBanner[];
}

const EventBanners: React.FC<EventBannersProps> = ({ banners }) => {
  const navigate = useNavigate();
  const [currentIndex, setCurrentIndex] = useState(0);

  const nextSlide = useCallback(() => {
    setCurrentIndex((prev) => (prev + 1) % banners.length);
  }, [banners.length]);

  useEffect(() => {
    if (banners.length <= 1) return;

    const interval = setInterval(nextSlide, 3000);
    return () => clearInterval(interval);
  }, [banners.length, nextSlide]);

  if (banners.length === 0) return null;

  const handleBannerClick = (banner: EventBanner) => {
    if (banner.linkUrl.startsWith('http')) {
      window.open(banner.linkUrl, '_blank');
    } else {
      navigate(banner.linkUrl);
    }
  };

  return (
    <Box>
      <Box
        sx={{
          position: 'relative',
          borderRadius: '12px',
          overflow: 'hidden',
          height: 120,
          cursor: 'pointer',
        }}
        onClick={() => handleBannerClick(banners[currentIndex])}
      >
        {banners[currentIndex].imageUrl ? (
          <Box
            component="img"
            src={banners[currentIndex].imageUrl!}
            alt={banners[currentIndex].title}
            sx={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
            }}
          />
        ) : (
          <Box
            sx={{
              width: '100%',
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: banners[currentIndex].type === 'EVENT'
                ? 'linear-gradient(135deg, #FFE0B2 0%, #FFCC80 100%)'
                : 'linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%)',
              px: 3,
            }}
          >
            <Typography
              sx={{
                fontSize: '16px',
                fontWeight: 700,
                color: banners[currentIndex].type === 'EVENT' ? '#E65100' : '#1565C0',
                textAlign: 'center',
              }}
            >
              {banners[currentIndex].title}
            </Typography>
          </Box>
        )}
      </Box>

      {banners.length > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', gap: 0.75, mt: 1 }}>
          {banners.map((_, index) => (
            <Box
              key={index}
              sx={{
                width: 8,
                height: 8,
                borderRadius: '50%',
                backgroundColor: index === currentIndex ? '#424242' : '#E0E0E0',
                transition: 'background-color 0.3s',
              }}
            />
          ))}
        </Box>
      )}
    </Box>
  );
};

export default EventBanners;
