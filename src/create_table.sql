CREATE TABLE IF NOT EXISTS payments (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(100) UNIQUE,  -- Prevents duplicate names
    amount_owed DOUBLE,
    amount_paid DOUBLE
    );
