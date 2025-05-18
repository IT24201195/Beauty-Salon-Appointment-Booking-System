-- Sample data for initial users
-- Passwords are 'password' encoded with BCrypt
INSERT INTO users (username, password, full_name, email, phone_number, specialty, role)
VALUES 
('admin', '$2a$10$3gFsiHC3c7wMLyIJRK2QIu7OGlO9YwbVKpKS0qVZWJr03NNxrhVSu', 'Admin User', 'admin@mathra.com', '1234567890', 'Management', 'ADMIN'),
('staff', '$2a$10$3gFsiHC3c7wMLyIJRK2QIu7OGlO9YwbVKpKS0qVZWJr03NNxrhVSu', 'Staff User', 'staff@mathra.com', '1234567891', 'Hair Styling', 'STAFF'),
('user', '$2a$10$3gFsiHC3c7wMLyIJRK2QIu7OGlO9YwbVKpKS0qVZWJr03NNxrhVSu', 'Regular User', 'user@example.com', '1234567892', NULL, 'CUSTOMER')
ON CONFLICT (username) DO NOTHING; 
