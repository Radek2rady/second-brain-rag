-- data.sql - Initial Demo Users
-- Passwords are all: 'password' (hashed via BCrypt)

INSERT INTO users (username, hashed_password, role, department_id) VALUES 
('alice', '$2a$10$X8OOM8m.E8aL.qFByfONj.Q7V7e.L8c6p.O.K.64L8b.2qE/Y1J7u', 'ADMIN', 'IT'),
('bob', '$2a$10$X8OOM8m.E8aL.qFByfONj.Q7V7e.L8c6p.O.K.64L8b.2qE/Y1J7u', 'ADMIN', 'IT'),
('jan', '$2a$10$X8OOM8m.E8aL.qFByfONj.Q7V7e.L8c6p.O.K.64L8b.2qE/Y1J7u', 'USER', 'LEGAL'),
('katka', '$2a$10$X8OOM8m.E8aL.qFByfONj.Q7V7e.L8c6p.O.K.64L8b.2qE/Y1J7u', 'USER', 'LEGAL')
ON CONFLICT (username) DO NOTHING;
