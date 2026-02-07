-- pets 테이블에 누락 필드 추가 (nullable로 추가 - 기존 데이터 호환)
ALTER TABLE pets ADD COLUMN weight DECIMAL(5,2);
ALTER TABLE pets ADD COLUMN vaccination_status VARCHAR(200);
ALTER TABLE pets ADD COLUMN allergies VARCHAR(500);
ALTER TABLE pets ADD COLUMN special_notes TEXT;

-- pet_checklists 테이블 생성
CREATE TABLE pet_checklists (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL UNIQUE REFERENCES pets(id),
    friendly_to_strangers INTEGER NOT NULL CHECK (friendly_to_strangers BETWEEN 1 AND 5),
    friendly_to_dogs INTEGER NOT NULL CHECK (friendly_to_dogs BETWEEN 1 AND 5),
    friendly_to_cats INTEGER NOT NULL CHECK (friendly_to_cats BETWEEN 1 AND 5),
    activity_level INTEGER NOT NULL CHECK (activity_level BETWEEN 1 AND 5),
    barking_level INTEGER NOT NULL CHECK (barking_level BETWEEN 1 AND 5),
    separation_anxiety INTEGER NOT NULL CHECK (separation_anxiety BETWEEN 1 AND 5),
    house_training INTEGER NOT NULL CHECK (house_training BETWEEN 1 AND 5),
    command_training INTEGER NOT NULL CHECK (command_training BETWEEN 1 AND 5),
    eating_habit VARCHAR(50),
    walk_preference VARCHAR(50),
    fear_items VARCHAR(500),
    additional_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_pet_checklists_pet_id ON pet_checklists(pet_id);
