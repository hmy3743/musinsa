CREATE TABLE product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    brand_id VARCHAR(255) NOT NULL CHECK (brand_id = UPPER(brand_id)),
    category_id VARCHAR(255) NOT NULL CHECK (category_id = UPPER(category_id)),
    price DECIMAL(10, 2) NOT NULL
);