-- data.sql - Initial Demo Users
-- Passwords are all: 'password' (hashed via BCrypt)

INSERT INTO users (username, hashed_password, role, department_id) VALUES 
('alice', '$2a$10$qXIMPSqQSdGpM5PDncAWT.tK/MHIsrO66sW17LJGqPK8EEdm/E9qG', 'ROLE_ADMIN', 'IT'),
('bob', '$2a$10$qXIMPSqQSdGpM5PDncAWT.tK/MHIsrO66sW17LJGqPK8EEdm/E9qG', 'ROLE_ADMIN', 'IT'),
('jan', '$2a$10$qXIMPSqQSdGpM5PDncAWT.tK/MHIsrO66sW17LJGqPK8EEdm/E9qG', 'ROLE_USER', 'LEGAL'),
('katka', '$2a$10$qXIMPSqQSdGpM5PDncAWT.tK/MHIsrO66sW17LJGqPK8EEdm/E9qG', 'ROLE_USER', 'LEGAL')
ON CONFLICT (username) DO UPDATE SET 
hashed_password = EXCLUDED.hashed_password,
role = EXCLUDED.role,
department_id = EXCLUDED.department_id;
