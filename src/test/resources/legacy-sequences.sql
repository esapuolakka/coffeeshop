-- The sequences an older version of the app (Hibernate's default for GenerationType.AUTO)
-- left behind in a database volume: they start at 1 and nothing has been drawn from them.
CREATE SEQUENCE IF NOT EXISTS item_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS category_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS manufacturer_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS supplier_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS role_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS discount_seq START WITH 1 INCREMENT BY 50;
