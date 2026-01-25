/**
 * @fileoverview 주소 검색 컴포넌트
 */

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useGoogleMaps } from '../../hooks/useGoogleMaps';
import { GeocodingResult, PlacePrediction } from '../../types/location';

interface AddressSearchProps {
  /** 주소 선택 핸들러 */
  onSelect: (result: GeocodingResult) => void;
  /** 플레이스홀더 */
  placeholder?: string;
  /** 초기값 */
  defaultValue?: string;
  /** 커스텀 클래스 */
  className?: string;
  /** 비활성화 */
  disabled?: boolean;
}

/**
 * 주소 검색 (자동완성) 컴포넌트
 */
const AddressSearch: React.FC<AddressSearchProps> = ({
  onSelect,
  placeholder = '주소를 입력하세요',
  defaultValue = '',
  className = '',
  disabled = false,
}) => {
  const { isLoaded } = useGoogleMaps();
  const [query, setQuery] = useState(defaultValue);
  const [suggestions, setSuggestions] = useState<PlacePrediction[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const autocompleteServiceRef = useRef<google.maps.places.AutocompleteService | null>(null);
  const placesServiceRef = useRef<google.maps.places.PlacesService | null>(null);
  const debounceRef = useRef<NodeJS.Timeout | null>(null);

  // 서비스 초기화
  useEffect(() => {
    if (isLoaded && !autocompleteServiceRef.current) {
      autocompleteServiceRef.current = new google.maps.places.AutocompleteService();
      // PlacesService는 DOM 요소가 필요
      const div = document.createElement('div');
      placesServiceRef.current = new google.maps.places.PlacesService(div);
    }
  }, [isLoaded]);

  // 자동완성 검색
  const searchPlaces = useCallback(
    async (input: string) => {
      if (!autocompleteServiceRef.current || input.length < 2) {
        setSuggestions([]);
        return;
      }

      setIsLoading(true);

      try {
        const response = await new Promise<google.maps.places.AutocompletePrediction[]>(
          (resolve, reject) => {
            autocompleteServiceRef.current!.getPlacePredictions(
              {
                input,
                componentRestrictions: { country: 'kr' },
                types: ['address'],
              },
              (
                predictions: google.maps.places.AutocompletePrediction[] | null,
                status: google.maps.places.PlacesServiceStatus
              ) => {
                if (status === google.maps.places.PlacesServiceStatus.OK && predictions) {
                  resolve(predictions);
                } else {
                  resolve([]);
                }
              }
            );
          }
        );

        const mappedSuggestions: PlacePrediction[] = response.map((p) => ({
          placeId: p.place_id,
          description: p.description,
          mainText: p.structured_formatting.main_text,
          secondaryText: p.structured_formatting.secondary_text || '',
        }));

        setSuggestions(mappedSuggestions);
        setIsOpen(mappedSuggestions.length > 0);
      } catch (error) {
        console.error('Places search error:', error);
        setSuggestions([]);
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  // 디바운스된 검색
  useEffect(() => {
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    debounceRef.current = setTimeout(() => {
      searchPlaces(query);
    }, 300);

    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, [query, searchPlaces]);

  // 장소 상세 정보 조회
  const getPlaceDetails = useCallback(
    async (placeId: string): Promise<GeocodingResult | null> => {
      if (!placesServiceRef.current) return null;

      return new Promise((resolve) => {
        placesServiceRef.current!.getDetails(
          {
            placeId,
            fields: ['formatted_address', 'geometry', 'place_id', 'address_components'],
          },
          (
            place: google.maps.places.PlaceResult | null,
            status: google.maps.places.PlacesServiceStatus
          ) => {
            if (status === google.maps.places.PlacesServiceStatus.OK && place?.geometry?.location) {
              resolve({
                formattedAddress: place.formatted_address || '',
                latitude: place.geometry.location.lat(),
                longitude: place.geometry.location.lng(),
                placeId: place.place_id || placeId,
              });
            } else {
              resolve(null);
            }
          }
        );
      });
    },
    []
  );

  // 항목 선택
  const handleSelect = useCallback(
    async (prediction: PlacePrediction) => {
      setQuery(prediction.description);
      setIsOpen(false);
      setSuggestions([]);

      const details = await getPlaceDetails(prediction.placeId);
      if (details) {
        onSelect(details);
      }
    },
    [getPlaceDetails, onSelect]
  );

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
  };

  const handleInputFocus = () => {
    if (suggestions.length > 0) {
      setIsOpen(true);
    }
  };

  const handleInputBlur = () => {
    // 약간의 딜레이를 두어 클릭 이벤트가 먼저 처리되도록 함
    setTimeout(() => setIsOpen(false), 200);
  };

  if (!isLoaded) {
    return (
      <div className={`address-search ${className}`}>
        <input type="text" placeholder="로딩 중..." disabled className="address-search__input" />
      </div>
    );
  }

  return (
    <div className={`address-search ${className}`}>
      <div className="address-search__input-wrapper">
        <input
          type="text"
          value={query}
          onChange={handleInputChange}
          onFocus={handleInputFocus}
          onBlur={handleInputBlur}
          placeholder={placeholder}
          disabled={disabled}
          className="address-search__input"
          autoComplete="off"
        />
        {isLoading && <span className="address-search__spinner">검색 중...</span>}
      </div>

      {isOpen && suggestions.length > 0 && (
        <ul className="address-search__suggestions">
          {suggestions.map((suggestion) => (
            <li
              key={suggestion.placeId}
              className="address-search__suggestion-item"
              onClick={() => handleSelect(suggestion)}
            >
              <span className="address-search__main-text">{suggestion.mainText}</span>
              {suggestion.secondaryText && (
                <span className="address-search__secondary-text">{suggestion.secondaryText}</span>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default AddressSearch;
