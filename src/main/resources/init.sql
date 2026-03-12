DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'shop_admin') THEN
        CREATE ROLE shop_admin LOGIN PASSWORD 'admin123';
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'shop_guest') THEN
        CREATE ROLE shop_guest LOGIN PASSWORD 'guest123';
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    price NUMERIC(10, 2),
    in_stock BOOLEAN DEFAULT TRUE
);

GRANT CONNECT ON DATABASE shop_db TO shop_admin, shop_guest;
GRANT USAGE ON SCHEMA public TO shop_admin, shop_guest;

GRANT SELECT, INSERT, UPDATE, DELETE ON products TO shop_admin;
GRANT USAGE, SELECT ON SEQUENCE products_id_seq TO shop_admin;

GRANT SELECT ON products TO shop_guest;

CREATE OR REPLACE PROCEDURE sp_create_table()
LANGUAGE plpgsql AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS products (
        id SERIAL PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        brand VARCHAR(255) NOT NULL,
        category VARCHAR(100),
        price NUMERIC(10, 2),
        in_stock BOOLEAN DEFAULT TRUE
    );
END;
$$;

CREATE OR REPLACE PROCEDURE sp_drop_table()
LANGUAGE plpgsql AS $$
BEGIN
    DROP TABLE IF EXISTS products CASCADE;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_clear_table()
LANGUAGE plpgsql AS $$
BEGIN
    TRUNCATE TABLE products RESTART IDENTITY;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_add_product(
    p_name VARCHAR,
    p_brand VARCHAR,
    p_category VARCHAR,
    p_price NUMERIC,
    p_in_stock BOOLEAN
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO products(name, brand, category, price, in_stock)
    VALUES (p_name, p_brand, p_category, p_price, p_in_stock);
END;
$$;

CREATE OR REPLACE FUNCTION fn_get_all_products()
RETURNS TABLE(id INT, name VARCHAR, brand VARCHAR, category VARCHAR, price NUMERIC, in_stock BOOLEAN)
LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
        SELECT p.id, p.name, p.brand, p.category, p.price, p.in_stock
        FROM products p ORDER BY p.id;
END;
$$;

CREATE OR REPLACE FUNCTION fn_search_by_brand(p_brand VARCHAR)
RETURNS TABLE(id INT, name VARCHAR, brand VARCHAR, category VARCHAR, price NUMERIC, in_stock BOOLEAN)
LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
        SELECT p.id, p.name, p.brand, p.category, p.price, p.in_stock
        FROM products p
        WHERE p.brand ILIKE '%' || p_brand || '%'
        ORDER BY p.id;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_update_product(
    p_id INT,
    p_name VARCHAR,
    p_brand VARCHAR,
    p_category VARCHAR,
    p_price NUMERIC,
    p_in_stock BOOLEAN
)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE products
    SET name = p_name, brand = p_brand, category = p_category,
        price = p_price, in_stock = p_in_stock
    WHERE id = p_id;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_delete_by_brand(p_brand VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    DELETE FROM products WHERE brand ILIKE '%' || p_brand || '%';
END;
$$;

CREATE OR REPLACE PROCEDURE sp_create_user(
    p_username VARCHAR,
    p_password VARCHAR,
    p_role VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_role VARCHAR;
BEGIN
    IF p_role = 'admin' THEN v_role := 'shop_admin';
    ELSE v_role := 'shop_guest';
    END IF;
    EXECUTE format('CREATE USER %I WITH PASSWORD %L', p_username, p_password);
    EXECUTE format('GRANT %I TO %I', v_role, p_username);
END;
$$;

GRANT EXECUTE ON PROCEDURE sp_create_table() TO shop_admin;
GRANT EXECUTE ON PROCEDURE sp_drop_table() TO shop_admin;
GRANT EXECUTE ON PROCEDURE sp_clear_table() TO shop_admin;
GRANT EXECUTE ON PROCEDURE sp_add_product(VARCHAR,VARCHAR,VARCHAR,NUMERIC,BOOLEAN) TO shop_admin;
GRANT EXECUTE ON FUNCTION  fn_get_all_products() TO shop_admin;
GRANT EXECUTE ON FUNCTION  fn_search_by_brand(VARCHAR) TO shop_admin;
GRANT EXECUTE ON PROCEDURE sp_update_product(INT,VARCHAR,VARCHAR,VARCHAR,NUMERIC,BOOLEAN) TO shop_admin;
GRANT EXECUTE ON PROCEDURE sp_delete_by_brand(VARCHAR) TO shop_admin;
GRANT EXECUTE ON PROCEDURE sp_create_user(VARCHAR,VARCHAR,VARCHAR) TO shop_admin;

GRANT EXECUTE ON FUNCTION fn_get_all_products() TO shop_guest;
GRANT EXECUTE ON FUNCTION fn_search_by_brand(VARCHAR) TO shop_guest;
