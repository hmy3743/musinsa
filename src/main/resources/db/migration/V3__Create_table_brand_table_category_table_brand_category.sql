CREATE TABLE category (
    id VARCHAR(255) NOT NULL CHECK (id = UPPER(id)) PRIMARY KEY
);

CREATE TABLE brand (
    id VARCHAR(255) NOT NULL CHECK (id = UPPER(id)) PRIMARY KEY
);

CREATE TABLE brand_category (
    brand_id VARCHAR(255) NOT NULL CHECK (brand_id = UPPER(brand_id)),
    category_id VARCHAR(255) NOT NULL CHECK (category_id = UPPER(category_id)),
    count INT NOT NULL,
    PRIMARY KEY (brand_id, category_id),
    FOREIGN KEY (brand_id) REFERENCES brand(id),
    FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT unique_brand_category UNIQUE (brand_id, category_id),
    CONSTRAINT positive_count CHECK (0 < count)
);

INSERT INTO category (id)
SELECT DISTINCT category_id FROM product;

INSERT INTO brand (id)
SELECT DISTINCT brand_id FROM product;

INSERT INTO brand_category (brand_id, category_id, count)
SELECT brand_id, category_id, COUNT(*) FROM product GROUP BY brand_id, category_id;